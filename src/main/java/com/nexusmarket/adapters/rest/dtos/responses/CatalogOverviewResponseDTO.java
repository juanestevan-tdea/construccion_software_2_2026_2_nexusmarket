package com.nexusmarket.adapters.rest.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOverviewResponseDTO {

    private List<ProductResponseDTO> products;
    private List<CategoryResponseDTO> categories;
    private long totalProducts;
    private long totalCategories;
}
