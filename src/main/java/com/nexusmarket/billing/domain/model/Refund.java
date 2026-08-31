package com.nexusmarket.billing.domain.model;

import com.nexusmarket.logistics.domain.model.Return;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reembolsos")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnRequest;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    public Refund() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Return getReturnRequest() { return returnRequest; }
    public void setReturnRequest(Return returnRequest) { this.returnRequest = returnRequest; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
}