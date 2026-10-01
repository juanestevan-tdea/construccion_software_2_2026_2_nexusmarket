package com.nexusmarket.adapters.persistence.jpa.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexusmarket.adapters.persistence.jpa.entities.BuyerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.BuyerJpaRepository;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderJpaMapper {

    private final BuyerJpaRepository buyerJpaRepository;
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

        BuyerJpaEntity buyer = null;
        if (domain.getBuyerId() != null) {
            buyer = buyerJpaRepository.findById(domain.getBuyerId())
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

