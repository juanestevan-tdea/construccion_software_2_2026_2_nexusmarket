package com.nexusmarket.adapters.useCases;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.ProductUseCasePort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.ProductDomainService;
import com.nexusmarket.domain.valueobjects.ProductType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductUseCaseImpl implements ProductUseCasePort {

    private final ProductDomainService productDomainService;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public Product createProduct(String sku, String name, String description, BigDecimal price,
            ProductType type, Long sellerId, Long categoryId) {
        return productDomainService.createProduct(sku, name, description, price, type, sellerId, categoryId);
    }

    @Override
    public Product getProductByIdOrThrow(Long id) {
        return productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    public List<Product> findAll() {
        return productRepositoryPort.findAll();
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return productRepositoryPort.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findBySeller(Long sellerId) {
        return productRepositoryPort.findBySellerId(sellerId);
    }

    @Override
    public List<Product> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepositoryPort.findByPriceBetween(min, max);
    }

    @Override
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
            ProductType type, Long categoryId) {
        return productDomainService.updateProduct(id, name, description, price, type, categoryId);
    }

    @Override
    public Product deactivateProduct(Long id) {
        return productDomainService.deactivateProduct(id);
    }
}
