package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.exceptions.WarehouseCapacityExceededException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseDomainService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Transactional
    public Warehouse createWarehouse(String name, String location, WarehouseType type, Integer capacity) {
        if (warehouseRepositoryPort.existsByName(name)) {
            throw new DuplicateResourceException("Warehouse", "name", name);
        }

        Warehouse warehouse = Warehouse.builder()
                .name(name)
                .location(location)
                .type(type)
                .capacity(capacity)
                .build();

        return warehouseRepositoryPort.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, String name, String location, WarehouseType type, Integer capacity) {
        Warehouse warehouse = warehouseRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        if (name != null && !name.isBlank()) {
            if (warehouseRepositoryPort.findByName(name)
                    .filter(w -> !w.getId().equals(id))
                    .isPresent()) {
                throw new DuplicateResourceException("Warehouse", "name", name);
            }
            warehouse.setName(name);
        }

        if (location != null && !location.isBlank()) {
            warehouse.setLocation(location);
        }

        if (type != null) {
            warehouse.setType(type);
        }

        if (capacity != null) {
            int currentStored = inventoryRepositoryPort.findByWarehouseId(id).stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
            if (capacity < currentStored) {
                throw new WarehouseCapacityExceededException(
                        String.format("Warehouse capacity cannot be set to %d because %d units are already stored",
                                capacity, currentStored));
            }
            warehouse.setCapacity(capacity);
        }

        return warehouseRepositoryPort.save(warehouse);
    }
}

