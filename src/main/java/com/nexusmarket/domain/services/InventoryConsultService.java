package com.nexusmarket.domain.services;

import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryConsultService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public Inventory getByIdOrThrow(Long id) {
        return inventoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    public List<Inventory> findAll() {
        return inventoryRepositoryPort.findAll();
    }

    public List<Inventory> findByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return inventoryRepositoryPort.findByProductId(productId);
    }

    public List<Inventory> findByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", warehouseId);
        }
        return inventoryRepositoryPort.findByWarehouseId(warehouseId);
    }
}
