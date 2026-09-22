package com.nexusmarket.catalog.dto;

import com.nexusmarket.catalog.domain.model.WarehouseType;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUpdateRequest {

    private String name;
    private String location;
    private WarehouseType type;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
