package com.nexusmarket.adapters.persistence.jpa.entities;

import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compradores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserJpaEntity user;

    @Column(nullable = false)
    private String primaryAddress;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "comprador_direcciones_adicionales", joinColumns = @JoinColumn(name = "comprador_id"))
    @Column(name = "direccion")
    private List<String> additionalAddresses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuyerCommercialStatus commercialStatus;
}
