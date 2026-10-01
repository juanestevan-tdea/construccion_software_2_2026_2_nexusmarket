package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.domain.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final BuyerRepository buyerRepository;

    @Transactional
    public Order createOrder(Long buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", buyerId));

        if (buyer.getCommercialStatus() != BuyerCommercialStatus.ACTIVE) {
            throw new BusinessRuleException("Buyer must be active to place orders. Current status: " + buyer.getCommercialStatus());
        }

        if (buyer.getUser() != null && !buyer.getUser().isActive()) {
            throw new BusinessRuleException("User associated with buyer is not active");
        }

        Order order = Order.builder()
                .buyerId(buyer.getId())
                .buyerEmail(buyer.getUser() != null ? buyer.getUser().getEmail() : null)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CART)
                .totalAmount(BigDecimal.ZERO)
                .build();

        return orderRepositoryPort.save(order);
    }
}
