package com.nexusmarket.domain.models;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.valueobjects.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private InventoryStatus status;

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

