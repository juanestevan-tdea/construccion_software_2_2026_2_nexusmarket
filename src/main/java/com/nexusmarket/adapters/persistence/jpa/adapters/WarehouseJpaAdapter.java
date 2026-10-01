package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.WarehouseJpaRepository;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository warehouseJpaRepository;
    private final WarehouseJpaMapper warehouseJpaMapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        WarehouseJpaEntity entity = warehouseJpaMapper.toEntity(warehouse);
        WarehouseJpaEntity saved = warehouseJpaRepository.save(entity);
        return warehouseJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return warehouseJpaRepository.findById(id)
                .map(warehouseJpaMapper::toDomain);
    }

    @Override
    public Optional<Warehouse> findByName(String name) {
        return warehouseJpaRepository.findByName(name)
                .map(warehouseJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return warehouseJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return warehouseJpaRepository.existsByName(name);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseJpaRepository.findAll().stream()
                .map(warehouseJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Warehouse> findByType(WarehouseType type) {
        return warehouseJpaRepository.findByType(type).stream()
                .map(warehouseJpaMapper::toDomain)
                .toList();
    }
}
