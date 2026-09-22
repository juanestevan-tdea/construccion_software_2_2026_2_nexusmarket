package com.nexusmarket.catalog.dto;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.model.WarehouseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {

    private Long id;
    private String name;
    private String location;
    private WarehouseType type;
    private Integer capacity;

    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .type(warehouse.getType())
                .capacity(warehouse.getCapacity())
                .build();
    }
}
