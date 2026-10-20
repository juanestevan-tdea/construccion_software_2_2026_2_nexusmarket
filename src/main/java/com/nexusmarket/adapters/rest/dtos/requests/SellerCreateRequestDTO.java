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
public class SellerCreateRequestDTO {

    @NotBlank(message = "Tax ID is required")
    private String taxId;

    @NotBlank(message = "Company name is required")
    private String companyName;
}
