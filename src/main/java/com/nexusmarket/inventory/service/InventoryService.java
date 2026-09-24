package com.nexusmarket.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.common.exception.WarehouseCapacityExceededException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.model.InventoryStatus;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;
import com.nexusmarket.inventory.dto.InventoryCreateRequest;
import com.nexusmarket.inventory.dto.InventoryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public InventoryResponse createInventory(InventoryCreateRequest request) {
        Inventory inventory = createInventory(request.getProductId(), request.getWarehouseId(), request.getQuantity());
        return InventoryResponse.fromEntity(inventory);
    }

    @Transactional
    public Inventory createInventory(Long productId, Long warehouseId, Integer quantity) {
        if (quantity < 0) {
            throw new BusinessRuleException("Initial inventory quantity cannot be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        // Validar capacidad de la bodega
        int currentStored = inventoryRepository.findByWarehouse(warehouse).stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        if (warehouse.getCapacity() != null && (currentStored + quantity > warehouse.getCapacity())) {
            throw new WarehouseCapacityExceededException(
                    String.format("Warehouse capacity of %d exceeded. Current: %d, adding: %d",
                            warehouse.getCapacity(), currentStored, quantity)
            );
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(quantity);
        inventory.setStatus(InventoryStatus.AVAILABLE);

        return inventoryRepository.save(inventory);
    }

    public Inventory getByIdOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> findByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return inventoryRepository.findByProduct(product);
    }

    public List<Inventory> findByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));
        return inventoryRepository.findByWarehouse(warehouse);
    }

    @Transactional
    public Inventory reserve(Long id, int amount) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.reserve(amount);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory confirmPayment(Long id) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.confirmPayment();
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory markAsDamaged(Long id) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.markAsDamaged();
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory adjust(Long id, int amount) {
        Inventory inventory = getByIdOrThrow(id);
        inventory.adjust(amount);
        return inventoryRepository.save(inventory);
    }
}
