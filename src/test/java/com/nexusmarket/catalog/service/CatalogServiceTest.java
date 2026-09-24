package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.response.CatalogOverviewResponse;
import com.nexusmarket.catalog.dto.response.ProductResponse;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void getCatalog_Success() {
        Product p = Product.builder().id(1L).name("Laptop").sku("LP-1").price(BigDecimal.valueOf(999)).active(true).build();
        Category c = Category.builder().id(1L).name("Tech").build();

        when(productRepository.findByActiveTrue()).thenReturn(List.of(p));
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(c));

        CatalogOverviewResponse response = catalogService.getCatalog();

        assertNotNull(response);
        assertEquals(1, response.getTotalProducts());
        assertEquals(1, response.getTotalCategories());
    }

    @Test
    void searchProducts_Success() {
        Product p = Product.builder().id(1L).name("Laptop").sku("LP-1").price(BigDecimal.valueOf(999)).active(true).build();
        when(productRepository.searchProducts("lap")).thenReturn(List.of(p));

        List<ProductResponse> results = catalogService.searchProducts("lap");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    void getProductDetail_ThrowsResourceNotFoundException_WhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> catalogService.getProductDetail(99L));
    }
}
