package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.ReturnResponseDTO;
import com.nexusmarket.domain.models.Return;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReturnRestMapper {

    public ReturnResponseDTO toResponseDTO(Return domain) {
        if (domain == null) {
            return null;
        }
        return ReturnResponseDTO.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .reason(domain.getReason())
                .status(domain.getStatus())
                .requestedAt(domain.getRequestedAt())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }
}
