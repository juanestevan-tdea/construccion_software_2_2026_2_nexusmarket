package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.response.CatalogOverviewResponse;
import com.nexusmarket.catalog.dto.response.CategoryResponse;
import com.nexusmarket.catalog.dto.response.ProductResponse;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CatalogOverviewResponse getCatalog() {
        List<ProductResponse> products = productRepository.findByActiveTrue().stream()
                .map(ProductResponse::fromEntity)
                .toList();

        List<CategoryResponse> categories = categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponse::fromEntity)
                .toList();

        return CatalogOverviewResponse.builder()
                .products(products)
                .categories(categories)
                .totalProducts(products.size())
                .totalCategories(categories.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return productRepository.findByActiveTrue().stream()
                    .map(ProductResponse::fromEntity)
                    .toList();
        }
        return productRepository.searchProducts(query.trim()).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ProductResponse.fromEntity(product);
    }
}
