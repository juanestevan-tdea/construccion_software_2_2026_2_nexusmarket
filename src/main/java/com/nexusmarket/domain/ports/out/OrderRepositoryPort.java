package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);

    boolean existsById(Long id);

    List<Order> findAll();

    List<Order> findByBuyerId(Long buyerId);
}
