package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import com.nexusmarket.domain.models.Warehouse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WarehouseRestMapper {

    public WarehouseResponseDTO toResponseDTO(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        return WarehouseResponseDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .location(domain.getLocation())
                .type(domain.getType())
                .capacity(domain.getCapacity())
                .build();
    }
}
