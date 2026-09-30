package com.nexusmarket.adapters.persistence.jpa.entities;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import com.nexusmarket.orders.domain.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
