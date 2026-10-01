package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerResponseDTO {

    private Long id;
    private UserResponseDTO user;
    private String primaryAddress;
    private List<String> additionalAddresses;
    private BuyerCommercialStatus commercialStatus;
}
