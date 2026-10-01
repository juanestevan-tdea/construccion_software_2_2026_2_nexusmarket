package com.nexusmarket.domain.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryConsultService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    public Inventory getByIdOrThrow(Long id) {
        return inventoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    public List<Inventory> findAll() {
        return inventoryRepositoryPort.findAll();
    }

    public List<Inventory> findByProduct(Long productId) {
        if (!productRepositoryPort.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return inventoryRepositoryPort.findByProductId(productId);
    }

    public List<Inventory> findByWarehouse(Long warehouseId) {
        if (!warehouseRepositoryPort.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", warehouseId);
        }
        return inventoryRepositoryPort.findByWarehouseId(warehouseId);
    }
}
