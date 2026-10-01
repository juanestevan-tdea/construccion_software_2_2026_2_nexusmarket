package com.nexusmarket.adapters.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.InventoryJpaRepository;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final InventoryJpaRepository inventoryJpaRepository;
    private final InventoryJpaMapper inventoryJpaMapper;

    @Override
    public Inventory save(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryJpaEntity entity = inventoryJpaMapper.toEntity(inventory);
        InventoryJpaEntity saved = inventoryJpaRepository.save(entity);
        return inventoryJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryJpaRepository.findById(id)
                .map(inventoryJpaMapper::toDomain);
    }

    @Override
    public List<Inventory> findAll() {
        return inventoryJpaRepository.findAll().stream()
                .map(inventoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findByProductId(Long productId) {
        return inventoryJpaRepository.findByProductId(productId).stream()
                .map(inventoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findByWarehouseId(Long warehouseId) {
        return inventoryJpaRepository.findByWarehouseId(warehouseId).stream()
                .map(inventoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return inventoryJpaRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(inventoryJpaMapper::toDomain);
    }
}
