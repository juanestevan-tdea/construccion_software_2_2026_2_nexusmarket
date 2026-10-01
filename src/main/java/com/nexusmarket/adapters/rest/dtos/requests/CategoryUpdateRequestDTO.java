package com.nexusmarket.adapters.rest.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequestDTO {

    private String name;
    private String description;
    private Long parentId;
}
