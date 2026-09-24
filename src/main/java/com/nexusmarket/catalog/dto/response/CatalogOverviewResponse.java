package com.nexusmarket.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOverviewResponse {

    private List<ProductResponse> products;
    private List<CategoryResponse> categories;
    private long totalProducts;
    private long totalCategories;
}
