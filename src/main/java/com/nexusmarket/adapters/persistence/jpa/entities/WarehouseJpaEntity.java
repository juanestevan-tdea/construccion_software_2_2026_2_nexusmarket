package com.nexusmarket.adapters.persistence.jpa.entities;

import com.nexusmarket.domain.valueobjects.WarehouseType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bodegas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseType type;

    @Column(nullable = false)
    private Integer capacity;
}
