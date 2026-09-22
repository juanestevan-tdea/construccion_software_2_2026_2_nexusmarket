package com.nexusmarket.inventory.dto;

import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.model.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private InventoryStatus status;

    public static InventoryResponse fromEntity(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
                .productName(inventory.getProduct() != null ? inventory.getProduct().getName() : null)
                .productSku(inventory.getProduct() != null ? inventory.getProduct().getSku() : null)
                .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
                .quantity(inventory.getQuantity())
                .status(inventory.getStatus())
                .build();
    }
}
