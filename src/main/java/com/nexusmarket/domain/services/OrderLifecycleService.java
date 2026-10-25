package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderLifecycleService {

    private final OrderRepositoryPort orderRepositoryPort;

    public Order checkout(Long id) {
        Order order = getByIdOrThrow(id);
        order.checkout();
        return orderRepositoryPort.save(order);
    }

    public Order confirmPayment(Long id) {
        Order order = getByIdOrThrow(id);
        order.confirmPayment();
        return orderRepositoryPort.save(order);
    }

    public Order dispatch(Long id) {
        Order order = getByIdOrThrow(id);
        order.dispatch();
        return orderRepositoryPort.save(order);
    }

    public Order deliver(Long id) {
        Order order = getByIdOrThrow(id);
        order.deliver();
        return orderRepositoryPort.save(order);
    }

    public Order finish(Long id) {
        Order order = getByIdOrThrow(id);
        order.finish();
        return orderRepositoryPort.save(order);
    }

    public Order cancel(Long id) {
        Order order = getByIdOrThrow(id);
        order.cancel();
        return orderRepositoryPort.save(order);
    }

    private Order getByIdOrThrow(Long id) {
        return orderRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}

