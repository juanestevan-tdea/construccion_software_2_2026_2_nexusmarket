package com.nexusmarket.adapters.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.ProductJpaRepository;
import com.nexusmarket.adapters.persistence.jpa.repositories.WarehouseJpaRepository;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Inventory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryJpaMapper {

    private final ProductJpaRepository productJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;

    public Inventory toDomain(InventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Inventory.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .productSku(entity.getProduct() != null ? entity.getProduct().getSku() : null)
                .warehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : null)
                .warehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null)
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .build();
    }

    public InventoryJpaEntity toEntity(Inventory domain) {
        if (domain == null) {
            return null;
        }

        ProductJpaEntity product = null;
        if (domain.getProductId() != null) {
            product = productJpaRepository.findById(domain.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", domain.getProductId()));
        }

        WarehouseJpaEntity warehouse = null;
        if (domain.getWarehouseId() != null) {
            warehouse = warehouseJpaRepository.findById(domain.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", domain.getWarehouseId()));
        }

        return InventoryJpaEntity.builder()
                .id(domain.getId())
                .product(product)
                .warehouse(warehouse)
                .quantity(domain.getQuantity())
                .status(domain.getStatus())
                .build();
    }
}


