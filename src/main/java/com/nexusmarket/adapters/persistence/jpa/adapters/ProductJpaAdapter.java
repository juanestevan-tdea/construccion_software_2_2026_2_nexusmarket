package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.ProductJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.ProductJpaRepository;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductJpaMapper productJpaMapper;

    @Override
    public Product save(Product product) {
        if (product == null) {
            return null;
        }
        ProductJpaEntity entity = productJpaMapper.toEntity(product);
        ProductJpaEntity saved = productJpaRepository.save(entity);
        return productJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(productJpaMapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findBySku(sku)
                .map(productJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return productJpaRepository.existsById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return productJpaRepository.existsBySku(sku);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByActiveTrue() {
        return productJpaRepository.findByActiveTrue().stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productJpaRepository.findByCategoryId(categoryId).stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategoryIdAndActiveTrue(Long categoryId) {
        return productJpaRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        return productJpaRepository.findBySellerId(sellerId).stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByPriceBetween(BigDecimal min, BigDecimal max) {
        return productJpaRepository.findByPriceBetween(min, max).stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productJpaRepository.searchProducts(query).stream()
                .map(productJpaMapper::toDomain)
                .toList();
    }
}
