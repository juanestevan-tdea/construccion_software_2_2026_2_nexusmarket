package com.nexusmarket.adapters.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.OrderJpaRepository;
import com.nexusmarket.adapters.persistence.jpa.repositories.WarehouseJpaRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Shipment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShipmentJpaMapper {

    private final OrderJpaRepository orderJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;

    public Shipment toDomain(ShipmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Shipment.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .warehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : null)
                .warehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null)
                .trackingNumber(entity.getTrackingNumber())
                .status(entity.getStatus())
                .shippedAt(entity.getShippedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    public ShipmentJpaEntity toEntity(Shipment domain) {
        if (domain == null) {
            return null;
        }

        OrderJpaEntity order = null;
        if (domain.getOrderId() != null) {
            order = orderJpaRepository.findById(domain.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", domain.getOrderId()));
        }

        WarehouseJpaEntity warehouse = null;
        if (domain.getWarehouseId() != null) {
            warehouse = warehouseJpaRepository.findById(domain.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", domain.getWarehouseId()));
        }

        return ShipmentJpaEntity.builder()
                .id(domain.getId())
                .order(order)
                .warehouse(warehouse)
                .trackingNumber(domain.getTrackingNumber())
                .status(domain.getStatus())
                .shippedAt(domain.getShippedAt())
                .deliveredAt(domain.getDeliveredAt())
                .build();
    }
}
