package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductType type;
    private Boolean active;
    private Long sellerId;
    private String sellerCompanyName;
    private Long categoryId;
    private String categoryName;
}
