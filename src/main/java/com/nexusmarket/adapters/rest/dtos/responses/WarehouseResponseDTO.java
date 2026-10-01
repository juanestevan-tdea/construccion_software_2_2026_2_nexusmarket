package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.WarehouseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {

    private Long id;
    private String name;
    private String location;
    private WarehouseType type;
    private Integer capacity;
}
