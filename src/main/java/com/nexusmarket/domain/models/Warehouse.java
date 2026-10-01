package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.WarehouseType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    private Long id;
    private String name;
    private String location;
    private WarehouseType type;
    private Integer capacity;
}
