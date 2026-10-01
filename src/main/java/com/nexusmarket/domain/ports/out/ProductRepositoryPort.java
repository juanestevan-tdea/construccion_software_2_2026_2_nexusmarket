package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findBySku(String sku);

    boolean existsById(Long id);

    boolean existsBySku(String sku);

    List<Product> findAll();

    List<Product> findByActiveTrue();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> searchProducts(String query);
}
