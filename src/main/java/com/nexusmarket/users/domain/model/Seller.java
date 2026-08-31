package com.nexusmarket.users.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vendedores")
@Data
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