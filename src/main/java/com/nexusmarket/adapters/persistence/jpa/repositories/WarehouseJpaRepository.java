package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import com.nexusmarket.domain.valueobjects.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseJpaRepository extends JpaRepository<WarehouseJpaEntity, Long> {

    Optional<WarehouseJpaEntity> findByName(String name);

    boolean existsByName(String name);

    List<WarehouseJpaEntity> findByType(WarehouseType type);
}
