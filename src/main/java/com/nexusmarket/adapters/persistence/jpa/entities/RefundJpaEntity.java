package com.nexusmarket.adapters.persistence.jpa.entities;

import java.math.BigDecimal;

import com.nexusmarket.domain.valueobjects.RefundStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reembolsos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceJpaEntity invoice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id")
    private ReturnJpaEntity returnRequest;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;
}
