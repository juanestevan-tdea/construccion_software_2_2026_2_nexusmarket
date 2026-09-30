package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Inventory;

import java.util.List;

public interface InventoryUseCasePort {

    Inventory createInventory(Long productId, Long warehouseId, Integer quantity);

    Inventory getByIdOrThrow(Long id);

    List<Inventory> findAll();

    List<Inventory> findByProduct(Long productId);

    List<Inventory> findByWarehouse(Long warehouseId);

    Inventory reserve(Long id, int amount);

    Inventory confirmPayment(Long id);

    Inventory markAsDamaged(Long id);

    Inventory adjust(Long id, int amount);
}
