package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import com.nexusmarket.domain.models.Product;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductRestMapper {

    public ProductResponseDTO toResponseDTO(Product domain) {
        if (domain == null) {
            return null;
        }
        return ProductResponseDTO.builder()
                .id(domain.getId())
                .sku(domain.getSku())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .type(domain.getType())
                .active(domain.getActive())
                .sellerId(domain.getSellerId())
                .sellerCompanyName(domain.getSellerCompanyName())
                .categoryId(domain.getCategoryId())
                .categoryName(domain.getCategoryName())
                .build();
    }
}
