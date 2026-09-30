package com.nexusmarket.domain.services;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.common.exception.ShipmentAlreadyExistsException;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentCreateService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public Shipment createShipment(Long orderId, Long warehouseId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new BusinessRuleException("Cannot create shipment for order that is not DISPATCHED. Current: " + order.getStatus());
        }

        if (shipmentRepositoryPort.existsByOrderId(orderId)) {
            throw new ShipmentAlreadyExistsException("Shipment", "orderId", orderId);
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        String tracking = trackingNumber;
        if (tracking == null || tracking.isBlank()) {
            tracking = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        } else if (shipmentRepositoryPort.existsByTrackingNumber(tracking)) {
            throw new ShipmentAlreadyExistsException("Shipment", "trackingNumber", tracking);
        }

        Shipment shipment = Shipment.builder()
                .orderId(order.getId())
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .trackingNumber(tracking)
                .status(ShipmentStatus.PENDING)
                .shippedAt(LocalDateTime.now())
                .build();

        return shipmentRepositoryPort.save(shipment);
    }
}
