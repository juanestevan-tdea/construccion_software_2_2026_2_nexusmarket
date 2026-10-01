package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;

@Repository
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, Long> {

    List<InventoryJpaEntity> findByProduct(ProductJpaEntity product);

    List<InventoryJpaEntity> findByWarehouse(WarehouseJpaEntity warehouse);

    Optional<InventoryJpaEntity> findByProductAndWarehouse(ProductJpaEntity product, WarehouseJpaEntity warehouse);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.product.id = :productId")
    List<InventoryJpaEntity> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.warehouse.id = :warehouseId")
    List<InventoryJpaEntity> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
    Optional<InventoryJpaEntity> findByProductIdAndWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
