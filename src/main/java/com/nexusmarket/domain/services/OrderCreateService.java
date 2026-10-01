package com.nexusmarket.domain.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public Order createOrder(Long buyerId) {
        Buyer buyer = buyerRepositoryPort.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", buyerId));

        if (buyer.getCommercialStatus() != BuyerCommercialStatus.ACTIVE) {
            throw new BusinessRuleException("Buyer must be active to place orders. Current status: " + buyer.getCommercialStatus());
        }

        User user = null;
        if (buyer.getUserId() != null) {
            user = userRepositoryPort.findById(buyer.getUserId()).orElse(null);
        }

        if (user != null && !user.isActive()) {
            throw new BusinessRuleException("User associated with buyer is not active");
        }

        Order order = Order.builder()
                .buyerId(buyer.getId())
                .buyerEmail(user != null ? user.getEmail() : null)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CART)
                .totalAmount(BigDecimal.ZERO)
                .build();

        return orderRepositoryPort.save(order);
    }
}
