package com.nexusmarket.catalog.service;

import com.nexusmarket.adapters.useCases.CatalogUseCaseImpl;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.CatalogUseCasePort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    private CatalogUseCasePort catalogUseCase;

    @BeforeEach
    void setUp() {
        catalogUseCase = new CatalogUseCaseImpl(productRepositoryPort, categoryRepositoryPort);
    }

    @Test
    void getCatalog_Success() {
        Product p = Product.builder().id(1L).name("Laptop").sku("LP-1").price(BigDecimal.valueOf(999)).active(true).build();
        Category c = Category.builder().id(1L).name("Tech").build();

        when(productRepositoryPort.findByActiveTrue()).thenReturn(List.of(p));
        when(categoryRepositoryPort.findByParentIsNull()).thenReturn(List.of(c));

        List<Product> products = catalogUseCase.getActiveProducts();
        List<Category> categories = catalogUseCase.getRootCategories();

        assertNotNull(products);
        assertNotNull(categories);
        assertEquals(1, products.size());
        assertEquals(1, categories.size());
    }

    @Test
    void searchProducts_Success() {
        Product p = Product.builder().id(1L).name("Laptop").sku("LP-1").price(BigDecimal.valueOf(999)).active(true).build();
        when(productRepositoryPort.searchProducts("lap")).thenReturn(List.of(p));

        List<Product> results = catalogUseCase.searchProducts("lap");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    void getProductDetail_ThrowsResourceNotFoundException_WhenNotFound() {
        when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> catalogUseCase.getProductDetail(99L));
    }
}

