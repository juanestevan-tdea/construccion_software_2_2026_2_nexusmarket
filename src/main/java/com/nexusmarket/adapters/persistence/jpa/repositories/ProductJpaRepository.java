package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.CategoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    Optional<ProductJpaEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductJpaEntity> findByCategory(CategoryJpaEntity category);

    List<ProductJpaEntity> findBySeller(SellerJpaEntity seller);

    List<ProductJpaEntity> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<ProductJpaEntity> findByActiveTrue();

    List<ProductJpaEntity> findByCategoryAndActiveTrue(CategoryJpaEntity category);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.category.id = :categoryId")
    List<ProductJpaEntity> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.category.id = :categoryId AND p.active = true")
    List<ProductJpaEntity> findByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.seller.id = :sellerId")
    List<ProductJpaEntity> findBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.active = true AND "
            + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<ProductJpaEntity> searchProducts(@Param("query") String query);
}
