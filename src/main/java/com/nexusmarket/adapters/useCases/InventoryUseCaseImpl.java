package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.ports.in.InventoryUseCasePort;
import com.nexusmarket.domain.services.InventoryConsultService;
import com.nexusmarket.domain.services.InventoryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryUseCaseImpl implements InventoryUseCasePort {

    private final InventoryManagementService inventoryManagementService;
    private final InventoryConsultService inventoryConsultService;

    @Override
    public Inventory createInventory(Long productId, Long warehouseId, Integer quantity) {
        return inventoryManagementService.createInventory(productId, warehouseId, quantity);
    }

    @Override
    public Inventory getByIdOrThrow(Long id) {
        return inventoryConsultService.getByIdOrThrow(id);
    }

    @Override
    public List<Inventory> findAll() {
        return inventoryConsultService.findAll();
    }

    @Override
    public List<Inventory> findByProduct(Long productId) {
        return inventoryConsultService.findByProduct(productId);
    }

    @Override
    public List<Inventory> findByWarehouse(Long warehouseId) {
        return inventoryConsultService.findByWarehouse(warehouseId);
    }

    @Override
    public Inventory reserve(Long id, int amount) {
        return inventoryManagementService.reserve(id, amount);
    }

    @Override
    public Inventory confirmPayment(Long id) {
        return inventoryManagementService.confirmPayment(id);
    }

    @Override
    public Inventory markAsDamaged(Long id) {
        return inventoryManagementService.markAsDamaged(id);
    }

    @Override
    public Inventory adjust(Long id, int amount) {
        return inventoryManagementService.adjust(id, amount);
    }
}
