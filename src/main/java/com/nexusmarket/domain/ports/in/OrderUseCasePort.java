package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Order;

import java.util.List;

public interface OrderUseCasePort {

    Order createOrder(Long buyerId);

    Order getByIdOrThrow(Long id);

    List<Order> findAll();

    List<Order> findByBuyer(Long buyerId);

    Order addItem(Long orderId, Long productId, Integer quantity);

    Order removeItem(Long orderId, Long itemId);

    Order checkout(Long id);

    Order confirmPayment(Long id);

    Order dispatch(Long id);

    Order deliver(Long id);

    Order finish(Long id);

    Order cancel(Long id);
}
