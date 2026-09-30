package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, Long> {

    List<InventoryJpaEntity> findByProduct(Product product);

    List<InventoryJpaEntity> findByWarehouse(Warehouse warehouse);

    Optional<InventoryJpaEntity> findByProductAndWarehouse(Product product, Warehouse warehouse);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.product.id = :productId")
    List<InventoryJpaEntity> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.warehouse.id = :warehouseId")
    List<InventoryJpaEntity> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
    Optional<InventoryJpaEntity> findByProductIdAndWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
