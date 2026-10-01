package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemJpaMapper {

    private final ProductRepository productRepository;

    public OrderItem toDomain(OrderItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderItem.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .productSku(entity.getProduct() != null ? entity.getProduct().getSku() : null)
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .build();
    }

    public OrderItemJpaEntity toEntity(OrderItem domain, OrderJpaEntity orderEntity) {
        if (domain == null) {
            return null;
        }
        Product product = null;
        if (domain.getProductId() != null) {
            product = productRepository.findById(domain.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", domain.getProductId()));
        }

        return OrderItemJpaEntity.builder()
                .id(domain.getId())
                .order(orderEntity)
                .product(product)
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .build();
    }
}
