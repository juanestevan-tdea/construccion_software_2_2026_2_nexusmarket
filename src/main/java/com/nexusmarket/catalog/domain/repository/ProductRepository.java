package com.nexusmarket.catalog.domain.repository;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.users.domain.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> findByCategory(Category category);
    List<Product> findBySeller(Seller seller);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(Category category);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("query") String query);
}