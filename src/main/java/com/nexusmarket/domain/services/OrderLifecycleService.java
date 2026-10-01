package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLifecycleService {

    private final OrderRepositoryPort orderRepositoryPort;

    @Transactional
    public Order checkout(Long id) {
        Order order = getByIdOrThrow(id);
        order.checkout();
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order confirmPayment(Long id) {
        Order order = getByIdOrThrow(id);
        order.confirmPayment();
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order dispatch(Long id) {
        Order order = getByIdOrThrow(id);
        order.dispatch();
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order deliver(Long id) {
        Order order = getByIdOrThrow(id);
        order.deliver();
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order finish(Long id) {
        Order order = getByIdOrThrow(id);
        order.finish();
        return orderRepositoryPort.save(order);
    }

    @Transactional
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
