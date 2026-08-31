package com.nexusmarket.orders.domain.model;

import com.nexusmarket.users.domain.model.Buyer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    // Métodos de negocio
    public void confirmPayment() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order must be in PENDING_PAYMENT state");
        }
        this.status = OrderStatus.PAID;
    }

    public void dispatch() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be PAID to dispatch");
        }
        this.status = OrderStatus.DISPATCHED;
    }

    public void deliver() {
        if (this.status != OrderStatus.DISPATCHED) {
            throw new IllegalStateException("Order must be DISPATCHED to deliver");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void finish() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must be DELIVERED to finish");
        }
        this.status = OrderStatus.FINISHED;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Buyer getBuyer() { return buyer; }
    public void setBuyer(Buyer buyer) { this.buyer = buyer; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}