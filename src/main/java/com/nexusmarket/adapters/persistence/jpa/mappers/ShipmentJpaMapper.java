package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentJpaMapper {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;

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

        Order order = null;
        if (domain.getOrderId() != null) {
            order = orderRepository.findById(domain.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", domain.getOrderId()));
        }

        Warehouse warehouse = null;
        if (domain.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(domain.getWarehouseId())
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
