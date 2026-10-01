package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.ProductType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductType type;
    @Builder.Default
    private Boolean active = true;
    private Long sellerId;
    private String sellerCompanyName;
    private Long categoryId;
    private String categoryName;
}
