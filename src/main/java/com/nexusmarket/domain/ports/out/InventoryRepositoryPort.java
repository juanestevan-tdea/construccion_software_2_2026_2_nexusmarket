package com.nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.domain.models.Inventory;

public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(Long id);

    List<Inventory> findAll();

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByProduct(Product product);

    List<Inventory> findByWarehouse(Warehouse warehouse);
}

