package com.nexusmarket.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerCreateRequestDTO {

    @NotBlank(message = "Primary address is required")
    private String primaryAddress;
}
