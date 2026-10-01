package com.nexusmarket.domain.models;

import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private Long id;
    private Long buyerId;
    private String buyerEmail;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.CART && this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidStatusTransitionException(status.name(), "MODIFY_ITEMS");
        }
        item.setOrderId(this.id);
        this.items.add(item);
        recalculateTotal();
    }

    public void removeItem(Long itemId) {
        if (this.status != OrderStatus.CART && this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidStatusTransitionException(status.name(), "MODIFY_ITEMS");
        }
        this.items.removeIf(item -> item.getId() != null && item.getId().equals(itemId));
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = this.items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void checkout() {
        if (this.status != OrderStatus.CART) {
            throw new InvalidStatusTransitionException("CART", status.name());
        }
        if (this.items.isEmpty()) {
            throw new InvalidStatusTransitionException(status.name(), "PENDING_PAYMENT");
        }
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidStatusTransitionException("PENDING_PAYMENT", status.name());
        }
        this.status = OrderStatus.PAID;
    }

    public void dispatch() {
        if (this.status != OrderStatus.PAID) {
            throw new InvalidStatusTransitionException("PAID", status.name());
        }
        this.status = OrderStatus.DISPATCHED;
    }

    public void deliver() {
        if (this.status != OrderStatus.DISPATCHED) {
            throw new InvalidStatusTransitionException("DISPATCHED", status.name());
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void finish() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new InvalidStatusTransitionException("DELIVERED", status.name());
        }
        this.status = OrderStatus.FINISHED;
    }

    public void cancel() {
        if (this.status == OrderStatus.FINISHED || this.status == OrderStatus.DELIVERED) {
            throw new InvalidStatusTransitionException(status.name(), "CANCELLED");
        }
        this.status = OrderStatus.CANCELLED;
    }
}

