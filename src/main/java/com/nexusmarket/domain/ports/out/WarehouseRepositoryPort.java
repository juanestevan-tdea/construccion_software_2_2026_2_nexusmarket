package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.valueobjects.WarehouseType;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(Long id);

    Optional<Warehouse> findByName(String name);

    boolean existsById(Long id);

    boolean existsByName(String name);

    List<Warehouse> findAll();

    List<Warehouse> findByType(WarehouseType type);
}
