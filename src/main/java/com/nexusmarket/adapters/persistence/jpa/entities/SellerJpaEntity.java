package com.nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserJpaEntity user;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false)
    private String companyName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
