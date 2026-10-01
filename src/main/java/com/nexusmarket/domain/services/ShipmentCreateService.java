package com.nexusmarket.domain.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.exceptions.ShipmentAlreadyExistsException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentCreateService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    @Transactional
    public Shipment createShipment(Long orderId, Long warehouseId, String trackingNumber) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new BusinessRuleException("Cannot create shipment for order that is not DISPATCHED. Current: " + order.getStatus());
        }

        if (shipmentRepositoryPort.existsByOrderId(orderId)) {
            throw new ShipmentAlreadyExistsException("Shipment", "orderId", orderId);
        }

        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
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

