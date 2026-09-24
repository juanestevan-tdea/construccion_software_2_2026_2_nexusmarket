package com.nexusmarket.logistics.service;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.*;
import com.nexusmarket.logistics.domain.model.Shipment;
import com.nexusmarket.logistics.domain.model.ShipmentStatus;
import com.nexusmarket.logistics.domain.repository.ShipmentRepository;
import com.nexusmarket.logistics.dto.request.ShipmentCreateRequest;
import com.nexusmarket.logistics.dto.response.ShipmentResponse;
import com.nexusmarket.logistics.dto.request.ShipmentStatusUpdateRequest;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public ShipmentResponse createShipment(ShipmentCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        // Solo crear envío para órdenes en estado DISPATCHED
        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new BusinessRuleException("Cannot create shipment for order that is not DISPATCHED. Current: " + order.getStatus());
        }

        if (shipmentRepository.existsByOrder(order)) {
            throw new ShipmentAlreadyExistsException("Shipment", "orderId", order.getId());
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));

        String tracking = request.getTrackingNumber();
        if (tracking == null || tracking.isBlank()) {
            tracking = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        } else if (shipmentRepository.existsByTrackingNumber(tracking)) {
            throw new ShipmentAlreadyExistsException("Shipment", "trackingNumber", tracking);
        }

        Shipment shipment = Shipment.builder()
                .order(order)
                .warehouse(warehouse)
                .trackingNumber(tracking)
                .status(ShipmentStatus.PENDING)
                .shippedAt(LocalDateTime.now())
                .build();

        return ShipmentResponse.fromEntity(shipmentRepository.save(shipment));
    }

    @Transactional(readOnly = true)
    public Shipment getByIdOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentResponseById(Long id) {
        return ShipmentResponse.fromEntity(getByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findByTracking(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new TrackingNotFoundException("Shipment", "trackingNumber", trackingNumber));
        return ShipmentResponse.fromEntity(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Shipment shipment = shipmentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment for order", orderId));
        return ShipmentResponse.fromEntity(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> findAll() {
        return shipmentRepository.findAll().stream()
                .map(ShipmentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ShipmentResponse updateStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = getByIdOrThrow(id);

        if (shipment.getStatus() == ShipmentStatus.DELIVERED && request.getStatus() == ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("DELIVERED", "CANCELLED");
        }

        shipment.setStatus(request.getStatus());
        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        return ShipmentResponse.fromEntity(shipmentRepository.save(shipment));
    }
}
