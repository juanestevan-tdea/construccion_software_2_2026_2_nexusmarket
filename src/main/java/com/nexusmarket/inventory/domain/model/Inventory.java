package com.nexusmarket.inventory.domain.model;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import jakarta.persistence.*;

@Entity
@Table(name = "inventario")
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

    public Inventory() {}

    // Métodos de negocio
    public void reserve(int amount) {
        if (this.status == InventoryStatus.DAMAGED) {
            throw new IllegalStateException("Cannot reserve damaged inventory");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity");
        }
        this.quantity -= amount;
        this.status = InventoryStatus.RESERVED;
    }

    public void release(int amount) {
        this.quantity += amount;
        this.status = InventoryStatus.AVAILABLE;
    }

    public void adjust(int amount) {
        if (this.quantity + amount < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity += amount;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }
}