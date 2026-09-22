package com.nexusmarket.catalog.domain.repository;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.model.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
    boolean existsByName(String name);
    List<Warehouse> findByType(WarehouseType type);
}
