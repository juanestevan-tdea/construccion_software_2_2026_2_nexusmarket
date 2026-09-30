package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.ReturnJpaEntity;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnJpaMapper {

    private final OrderRepository orderRepository;

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

        Order order = null;
        if (domain.getOrderId() != null) {
            order = orderRepository.findById(domain.getOrderId())
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
