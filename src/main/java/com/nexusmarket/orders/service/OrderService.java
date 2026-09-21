package com.nexusmarket.orders.service;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.exception.BusinessRuleException;
import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderItem;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.domain.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(Long buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", buyerId));

        // Regla de Negocio RN-07: Comprador debe estar activo para realizar compras
        if (buyer.getCommercialStatus() != BuyerCommercialStatus.ACTIVE) {
            throw new BusinessRuleException("Buyer must be active to place orders. Current status: " + buyer.getCommercialStatus());
        }

        if (buyer.getUser() != null && !buyer.getUser().isActive()) {
            throw new BusinessRuleException("User associated with buyer is not active");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CART);
        order.setTotalAmount(BigDecimal.ZERO);

        return orderRepository.save(order);
    }

    public Order getByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByBuyer(Long buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", buyerId));
        return orderRepository.findByBuyer(buyer);
    }

    @Transactional
    public Order addItem(Long orderId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessRuleException("Item quantity must be greater than zero");
        }

        Order order = getByIdOrThrow(orderId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());

        order.addItem(item);
        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmPayment(Long id) {
        Order order = getByIdOrThrow(id);
        order.confirmPayment();
        return orderRepository.save(order);
    }

    @Transactional
    public Order dispatch(Long id) {
        Order order = getByIdOrThrow(id);
        order.dispatch();
        return orderRepository.save(order);
    }

    @Transactional
    public Order deliver(Long id) {
        Order order = getByIdOrThrow(id);
        order.deliver();
        return orderRepository.save(order);
    }

    @Transactional
    public Order finish(Long id) {
        Order order = getByIdOrThrow(id);
        order.finish();
        return orderRepository.save(order);
    }
}
