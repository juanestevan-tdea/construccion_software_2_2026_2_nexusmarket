package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO {

    private Long id;
    private Long orderId;
    private Long warehouseId;
    private String warehouseName;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
