package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    private Long id;
    private Long orderId;
    private Long warehouseId;
    private String warehouseName;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
