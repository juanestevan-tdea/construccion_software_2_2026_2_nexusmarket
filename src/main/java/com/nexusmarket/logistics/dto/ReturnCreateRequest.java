package com.nexusmarket.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnCreateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Return reason is required")
    private String reason;
}
