package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.OrderJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.OrderJpaRepository;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderJpaMapper orderJpaMapper;

    @Override
    public Order save(Order order) {
        if (order == null) {
            return null;
        }
        OrderJpaEntity entity = orderJpaMapper.toEntity(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return orderJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(orderJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return orderJpaRepository.existsById(id);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(orderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        return orderJpaRepository.findByBuyerId(buyerId).stream()
                .map(orderJpaMapper::toDomain)
                .toList();
    }
}
