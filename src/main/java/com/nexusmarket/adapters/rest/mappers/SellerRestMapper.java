package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SellerRestMapper {

    public SellerResponseDTO toResponseDTO(Seller domain, User user) {
        if (domain == null) {
            return null;
        }
        return SellerResponseDTO.builder()
                .id(domain.getId())
                .user(user != null ? UserRestMapper.toResponseDTO(user) : null)
                .taxId(domain.getTaxId())
                .companyName(domain.getCompanyName())
                .active(domain.getActive())
                .build();
    }
}
