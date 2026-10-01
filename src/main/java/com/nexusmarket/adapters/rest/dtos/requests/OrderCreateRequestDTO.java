package com.nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDTO {

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;
}
