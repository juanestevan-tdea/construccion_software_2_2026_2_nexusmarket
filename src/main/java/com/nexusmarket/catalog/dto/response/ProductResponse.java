package com.nexusmarket.catalog.dto.response;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

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

    public static ProductResponse fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .type(product.getType())
                .active(product.getActive())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerCompanyName(product.getSeller() != null ? product.getSeller().getCompanyName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }
}


