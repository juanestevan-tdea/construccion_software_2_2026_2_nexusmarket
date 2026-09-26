package com.nexusmarket.inventory.domain.model;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.common.exception.BusinessRuleException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    // Métodos de negocio
    public void reserve(int amount) {
        if (this.status == InventoryStatus.DAMAGED) {
            throw new BusinessRuleException("Cannot reserve damaged inventory");
        }
        if (this.quantity < amount) {
            throw new BusinessRuleException("Insufficient quantity");
        }
        this.quantity -= amount;
        this.status = InventoryStatus.RESERVED;
    }

    public void release(int amount) {
        this.quantity += amount;
        this.status = InventoryStatus.AVAILABLE;
    }

    public void markAsDamaged() {
        this.status = InventoryStatus.DAMAGED;
    }

    public void confirmPayment() {
        // La reserva ya descontó las unidades; confirmar el pago solo libera el estado
        if (this.status == InventoryStatus.DAMAGED) {
            throw new BusinessRuleException("Cannot confirm payment for damaged inventory");
        }
        this.status = InventoryStatus.AVAILABLE;
    }

    public void adjust(int amount) {
        if (this.quantity + amount < 0) {
            throw new BusinessRuleException("Quantity cannot be negative");
        }
        this.quantity += amount;
    }
}