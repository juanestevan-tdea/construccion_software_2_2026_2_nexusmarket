package com.nexusmarket.logistics.dto.response;

import com.nexusmarket.logistics.domain.model.Shipment;
import com.nexusmarket.logistics.domain.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private Long orderId;
    private Long warehouseId;
    private String warehouseName;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    public static ShipmentResponse fromEntity(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrder() != null ? shipment.getOrder().getId() : null)
                .warehouseId(shipment.getWarehouse() != null ? shipment.getWarehouse().getId() : null)
                .warehouseName(shipment.getWarehouse() != null ? shipment.getWarehouse().getName() : null)
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .build();
    }
}
