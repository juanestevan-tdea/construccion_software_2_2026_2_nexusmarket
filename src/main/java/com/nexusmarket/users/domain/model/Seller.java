package com.nexusmarket.users.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vendedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false)
    private String companyName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // Métodos de negocio
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}