package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.CatalogUseCasePort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogUseCaseImpl implements CatalogUseCasePort {

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public List<Product> getActiveProducts() {
        return productRepositoryPort.findByActiveTrue();
    }

    @Override
    public List<Category> getRootCategories() {
        return categoryRepositoryPort.findByParentIsNull();
    }

    @Override
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return productRepositoryPort.findByActiveTrue();
        }
        return productRepositoryPort.searchProducts(query.trim());
    }

    @Override
    public Product getProductDetail(Long id) {
        return productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}

