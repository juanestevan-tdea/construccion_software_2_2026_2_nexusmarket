package com.nexusmarket.domain.services;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCalculationService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductRepository productRepository;

    @Transactional
    public Order addItem(Long orderId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessRuleException("Item quantity must be greater than zero");
        }

        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();

        order.addItem(item);
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long itemId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.removeItem(itemId);
        return orderRepositoryPort.save(order);
    }
}
