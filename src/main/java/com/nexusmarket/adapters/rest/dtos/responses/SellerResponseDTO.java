package com.nexusmarket.adapters.rest.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    private Long id;
    private UserResponseDTO user;
    private String taxId;
    private String companyName;
    private Boolean active;
}
