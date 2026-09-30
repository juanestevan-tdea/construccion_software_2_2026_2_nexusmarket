package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import com.nexusmarket.domain.models.Inventory;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InventoryRestMapper {

    public InventoryResponseDTO toResponseDTO(Inventory domain) {
        if (domain == null) {
            return null;
        }
        return InventoryResponseDTO.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .productName(domain.getProductName())
                .productSku(domain.getProductSku())
                .warehouseId(domain.getWarehouseId())
                .warehouseName(domain.getWarehouseName())
                .quantity(domain.getQuantity())
                .status(domain.getStatus())
                .build();
    }
}
