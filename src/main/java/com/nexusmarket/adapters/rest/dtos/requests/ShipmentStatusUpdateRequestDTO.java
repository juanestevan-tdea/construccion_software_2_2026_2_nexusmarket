package com.nexusmarket.adapters.rest.dtos.requests;

import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdateRequestDTO {

    @NotNull(message = "Shipment status is required")
    private ShipmentStatus status;
}
