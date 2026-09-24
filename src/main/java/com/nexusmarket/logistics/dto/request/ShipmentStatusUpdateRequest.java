package com.nexusmarket.logistics.dto.request;

import com.nexusmarket.logistics.domain.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdateRequest {

    @NotNull(message = "Shipment status is required")
    private ShipmentStatus status;
}
