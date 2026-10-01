package com.nexusmarket.catalog.domain.model;

import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private SellerJpaEntity seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}