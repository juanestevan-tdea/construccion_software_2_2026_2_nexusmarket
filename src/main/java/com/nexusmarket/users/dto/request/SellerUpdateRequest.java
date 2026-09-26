package com.nexusmarket.users.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerUpdateRequest {

    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
}
