package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.request.CategoryCreateRequest;
import com.nexusmarket.catalog.dto.response.CategoryResponse;
import com.nexusmarket.common.exception.CategoryHasProductsException;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Books")
                .description("All kinds of books")
                .build();
    }

    @Test
    void createCategory_Success() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Books")
                .description("All kinds of books")
                .build();

        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Books", response.getName());
    }

    @Test
    void createCategory_ThrowsDuplicateResourceException_WhenNameExists() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Books")
                .build();

        when(categoryRepository.existsByName("Books")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void deleteCategory_ThrowsCategoryHasProductsException_WhenCategoryHasProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(category)).thenReturn(List.of(new Product()));

        assertThrows(CategoryHasProductsException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(category)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).delete(category);
    }

    @Test
    void getCategoryByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryByIdOrThrow(99L));
    }
}
