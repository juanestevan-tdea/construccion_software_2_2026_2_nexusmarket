package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.ShipmentResponseDTO;
import com.nexusmarket.domain.models.Shipment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ShipmentRestMapper {

    public ShipmentResponseDTO toResponseDTO(Shipment domain) {
        if (domain == null) {
            return null;
        }
        return ShipmentResponseDTO.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .warehouseId(domain.getWarehouseId())
                .warehouseName(domain.getWarehouseName())
                .trackingNumber(domain.getTrackingNumber())
                .status(domain.getStatus())
                .shippedAt(domain.getShippedAt())
                .deliveredAt(domain.getDeliveredAt())
                .build();
    }
}
