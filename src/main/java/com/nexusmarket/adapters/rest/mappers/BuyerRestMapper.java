package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;

@UtilityClass
public class BuyerRestMapper {

    public BuyerResponseDTO toResponseDTO(Buyer domain, User user) {
        if (domain == null) {
            return null;
        }
        return BuyerResponseDTO.builder()
                .id(domain.getId())
                .user(user != null ? UserRestMapper.toResponseDTO(user) : null)
                .primaryAddress(domain.getPrimaryAddress())
                .additionalAddresses(domain.getAdditionalAddresses() != null ? new ArrayList<>(domain.getAdditionalAddresses()) : new ArrayList<>())
                .commercialStatus(domain.getCommercialStatus())
                .build();
    }
}
