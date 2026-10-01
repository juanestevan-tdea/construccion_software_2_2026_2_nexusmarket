package com.nexusmarket.adapters.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.OrderUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.services.OrderCalculationService;
import com.nexusmarket.domain.services.OrderCreateService;
import com.nexusmarket.domain.services.OrderLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCasePort {

    private final OrderCreateService orderCreateService;
    private final OrderCalculationService orderCalculationService;
    private final OrderLifecycleService orderLifecycleService;
    private final OrderRepositoryPort orderRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;

    @Override
    public Order createOrder(Long buyerId) {
        return orderCreateService.createOrder(buyerId);
    }

    @Override
    public Order getByIdOrThrow(Long id) {
        return orderRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Override
    public List<Order> findAll() {
        return orderRepositoryPort.findAll();
    }

    @Override
    public List<Order> findByBuyer(Long buyerId) {
        if (!buyerRepositoryPort.existsById(buyerId)) {
            throw new ResourceNotFoundException("Buyer", buyerId);
        }
        return orderRepositoryPort.findByBuyerId(buyerId);
    }

    @Override
    public Order addItem(Long orderId, Long productId, Integer quantity) {
        return orderCalculationService.addItem(orderId, productId, quantity);
    }

    @Override
    public Order removeItem(Long orderId, Long itemId) {
        return orderCalculationService.removeItem(orderId, itemId);
    }

    @Override
    public Order checkout(Long id) {
        return orderLifecycleService.checkout(id);
    }

    @Override
    public Order confirmPayment(Long id) {
        return orderLifecycleService.confirmPayment(id);
    }

    @Override
    public Order dispatch(Long id) {
        return orderLifecycleService.dispatch(id);
    }

    @Override
    public Order deliver(Long id) {
        return orderLifecycleService.deliver(id);
    }

    @Override
    public Order finish(Long id) {
        return orderLifecycleService.finish(id);
    }

    @Override
    public Order cancel(Long id) {
        return orderLifecycleService.cancel(id);
    }
}

