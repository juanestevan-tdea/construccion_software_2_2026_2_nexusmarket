package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryJpaMapper {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

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

        Product product = null;
        if (domain.getProductId() != null) {
            product = productRepository.findById(domain.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", domain.getProductId()));
        }

        Warehouse warehouse = null;
        if (domain.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(domain.getWarehouseId())
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
