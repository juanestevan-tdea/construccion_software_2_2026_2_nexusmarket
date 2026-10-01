package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.CategoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.CategoryJpaRepository;
import com.nexusmarket.adapters.persistence.jpa.repositories.SellerJpaRepository;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductJpaMapper {

    private final CategoryJpaRepository categoryJpaRepository;
    private final SellerJpaRepository sellerJpaRepository;

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Product.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .type(entity.getType())
                .active(entity.getActive())
                .sellerId(entity.getSeller() != null ? entity.getSeller().getId() : null)
                .sellerCompanyName(entity.getSeller() != null ? entity.getSeller().getCompanyName() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .build();
    }

    public ProductJpaEntity toEntity(Product domain) {
        if (domain == null) {
            return null;
        }

        CategoryJpaEntity category = null;
        if (domain.getCategoryId() != null) {
            category = categoryJpaRepository.findById(domain.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", domain.getCategoryId()));
        }

        SellerJpaEntity seller = null;
        if (domain.getSellerId() != null) {
            seller = sellerJpaRepository.findById(domain.getSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seller", domain.getSellerId()));
        }

        return ProductJpaEntity.builder()
                .id(domain.getId())
                .sku(domain.getSku())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .type(domain.getType())
                .active(domain.getActive())
                .category(category)
                .seller(seller)
                .build();
    }
}

