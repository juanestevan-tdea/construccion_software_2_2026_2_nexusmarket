package com.nexusmarket.domain.ports.in;

import java.math.BigDecimal;
import java.util.List;

import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.valueobjects.ProductType;

public interface ProductUseCasePort {

    Product createProduct(String sku, String name, String description, BigDecimal price,
            ProductType type, Long sellerId, Long categoryId);

    Product getProductByIdOrThrow(Long id);

    List<Product> findAll();

    List<Product> findByCategory(Long categoryId);

    List<Product> findBySeller(Long sellerId);

    List<Product> findByPriceRange(BigDecimal min, BigDecimal max);

    Product updateProduct(Long id, String name, String description, BigDecimal price,
            ProductType type, Long categoryId);

    Product deactivateProduct(Long id);
}
