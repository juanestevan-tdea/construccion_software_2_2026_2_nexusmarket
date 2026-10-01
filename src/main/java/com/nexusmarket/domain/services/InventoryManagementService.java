package com.nexusmarket.domain.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.common.exception.WarehouseCapacityExceededException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.valueobjects.InventoryStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryManagementService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    @Transactional
    public Inventory createInventory(Long productId, Long warehouseId, Integer quantity) {
        if (quantity < 0) {
            throw new BusinessRuleException("Initial inventory quantity cannot be negative");
        }

        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        int currentStored = inventoryRepositoryPort.findByWarehouseId(warehouseId).stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        if (warehouse.getCapacity() != null && (currentStored + quantity > warehouse.getCapacity())) {
            throw new WarehouseCapacityExceededException(
                    String.format("Warehouse capacity of %d exceeded. Current: %d, adding: %d",
                            warehouse.getCapacity(), currentStored, quantity)
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .quantity(quantity)
                .status(InventoryStatus.AVAILABLE)
                .build();

        return inventoryRepositoryPort.save(inventory);
    }

    @Transactional
    public Inventory reserve(Long id, int amount) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.reserve(amount);
        return inventoryRepositoryPort.save(inventory);
    }

    @Transactional
    public Inventory confirmPayment(Long id) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.confirmPayment();
        return inventoryRepositoryPort.save(inventory);
    }

    @Transactional
    public Inventory markAsDamaged(Long id) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.markAsDamaged();
        return inventoryRepositoryPort.save(inventory);
    }

    @Transactional
    public Inventory adjust(Long id, int amount) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.adjust(amount);
        return inventoryRepositoryPort.save(inventory);
    }

    private Inventory getByIdOrThrow(Long id) {
        return inventoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }
}
