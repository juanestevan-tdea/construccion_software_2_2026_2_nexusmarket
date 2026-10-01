package com.nexusmarket.adapters.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ReturnJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.OrderJpaRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Return;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReturnJpaMapper {

    private final OrderJpaRepository orderJpaRepository;

    public Return toDomain(ReturnJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Return.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }

    public ReturnJpaEntity toEntity(Return domain) {
        if (domain == null) {
            return null;
        }

        OrderJpaEntity order = null;
        if (domain.getOrderId() != null) {
            order = orderJpaRepository.findById(domain.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", domain.getOrderId()));
        }

        return ReturnJpaEntity.builder()
                .id(domain.getId())
                .order(order)
                .reason(domain.getReason())
                .status(domain.getStatus())
                .requestedAt(domain.getRequestedAt())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }
}
