package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.model.WarehouseType;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.catalog.dto.WarehouseCreateRequest;
import com.nexusmarket.catalog.dto.WarehouseResponse;
import com.nexusmarket.catalog.dto.WarehouseUpdateRequest;
import com.nexusmarket.exception.DuplicateResourceException;
import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.exception.WarehouseCapacityExceededException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseCreateRequest request) {
        if (warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse", "name", request.getName());
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .type(request.getType())
                .capacity(request.getCapacity())
                .build();

        return WarehouseResponse.fromEntity(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public Warehouse getWarehouseByIdOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseResponseById(Long id) {
        return WarehouseResponse.fromEntity(getWarehouseByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAll() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findByType(WarehouseType type) {
        return warehouseRepository.findByType(type).stream()
                .map(WarehouseResponse::fromEntity)
                .toList();
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseUpdateRequest request) {
        Warehouse warehouse = getWarehouseByIdOrThrow(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (warehouseRepository.findByName(request.getName())
                    .filter(w -> !w.getId().equals(id))
                    .isPresent()) {
                throw new DuplicateResourceException("Warehouse", "name", request.getName());
            }
            warehouse.setName(request.getName());
        }

        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            warehouse.setLocation(request.getLocation());
        }

        if (request.getType() != null) {
            warehouse.setType(request.getType());
        }

        if (request.getCapacity() != null) {
            // Validar que la nueva capacidad no sea menor al total de ítems almacenados
            int currentStored = inventoryRepository.findByWarehouse(warehouse).stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
            if (request.getCapacity() < currentStored) {
                throw new WarehouseCapacityExceededException(
                        String.format("Warehouse capacity cannot be set to %d because %d units are already stored",
                                request.getCapacity(), currentStored));
            }
            warehouse.setCapacity(request.getCapacity());
        }

        return WarehouseResponse.fromEntity(warehouseRepository.save(warehouse));
    }
}
