package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;
import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderJpaMapper {

    private final BuyerRepository buyerRepository;
    private final OrderItemJpaMapper orderItemJpaMapper;

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<OrderItem> items = entity.getItems() != null
                ? entity.getItems().stream().map(orderItemJpaMapper::toDomain).toList()
                : Collections.emptyList();

        return Order.builder()
                .id(entity.getId())
                .buyerId(entity.getBuyer() != null ? entity.getBuyer().getId() : null)
                .buyerEmail(entity.getBuyer() != null && entity.getBuyer().getUser() != null
                        ? entity.getBuyer().getUser().getEmail() : null)
                .orderDate(entity.getOrderDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .items(new ArrayList<>(items))
                .build();
    }

    public OrderJpaEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }

        Buyer buyer = null;
        if (domain.getBuyerId() != null) {
            buyer = buyerRepository.findById(domain.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer", domain.getBuyerId()));
        }

        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(domain.getId())
                .buyer(buyer)
                .orderDate(domain.getOrderDate())
                .status(domain.getStatus())
                .totalAmount(domain.getTotalAmount())
                .items(new ArrayList<>())
                .build();

        if (domain.getItems() != null) {
            List<OrderItemJpaEntity> itemEntities = domain.getItems().stream()
                    .map(item -> orderItemJpaMapper.toEntity(item, entity))
                    .toList();
            entity.getItems().addAll(itemEntities);
        }

        return entity;
    }
}
