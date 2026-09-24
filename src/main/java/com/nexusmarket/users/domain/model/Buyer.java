package com.nexusmarket.users.domain.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compradores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

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

    // Métodos de negocio
    public void addAdditionalAddress(String address) {
        this.additionalAddresses.add(address);
    }

    public void removeAdditionalAddress(String address) {
        this.additionalAddresses.remove(address);
    }
} 

