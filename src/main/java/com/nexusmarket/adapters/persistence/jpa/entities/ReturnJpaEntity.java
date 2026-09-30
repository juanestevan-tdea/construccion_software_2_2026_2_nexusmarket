package com.nexusmarket.adapters.persistence.jpa.entities;

import com.nexusmarket.domain.valueobjects.ReturnStatus;
import com.nexusmarket.orders.domain.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;
}
