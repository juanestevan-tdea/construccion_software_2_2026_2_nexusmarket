package com.nexusmarket.orders.domain.model;

import com.nexusmarket.common.exception.InvalidStatusTransitionException;
import com.nexusmarket.users.domain.model.Buyer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Métodos de negocio
    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.CART && this.status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidStatusTransitionException(status.name(), "MODIFY_ITEMS");
        }
        item.setOrder(this);
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