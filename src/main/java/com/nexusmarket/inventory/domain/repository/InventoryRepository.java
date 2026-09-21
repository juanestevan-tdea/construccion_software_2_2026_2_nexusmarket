package com.nexusmarket.inventory.domain.repository;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.inventory.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProduct(Product product);
    List<Inventory> findByWarehouse(Warehouse warehouse);
    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);
}