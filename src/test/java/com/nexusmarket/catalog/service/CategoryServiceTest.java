package com.nexusmarket.catalog.service;

import com.nexusmarket.adapters.useCases.CategoryUseCaseImpl;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.CategoryUseCasePort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.CategoryDomainService;
import com.nexusmarket.domain.exceptions.CategoryHasProductsException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    private CategoryUseCasePort categoryUseCase;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Books")
                .description("All kinds of books")
                .build();

        CategoryDomainService domainService = new CategoryDomainService(categoryRepositoryPort, productRepositoryPort);
        categoryUseCase = new CategoryUseCaseImpl(domainService, categoryRepositoryPort);
    }

    @Test
    void createCategory_Success() {
        when(categoryRepositoryPort.existsByName("Books")).thenReturn(false);
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(category);

        Category response = categoryUseCase.createCategory("Books", "All kinds of books", null);

        assertNotNull(response);
        assertEquals("Books", response.getName());
    }

    @Test
    void createCategory_ThrowsDuplicateResourceException_WhenNameExists() {
        when(categoryRepositoryPort.existsByName("Books")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryUseCase.createCategory("Books", null, null));
    }

    @Test
    void deleteCategory_ThrowsCategoryHasProductsException_WhenCategoryHasProducts() {
        when(categoryRepositoryPort.findById(1L)).thenReturn(Optional.of(category));
        when(productRepositoryPort.findByCategoryId(1L)).thenReturn(List.of(new Product()));

        assertThrows(CategoryHasProductsException.class, () -> categoryUseCase.deleteCategory(1L));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepositoryPort.findById(1L)).thenReturn(Optional.of(category));
        when(productRepositoryPort.findByCategoryId(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> categoryUseCase.deleteCategory(1L));
        verify(categoryRepositoryPort).delete(category);
    }

    @Test
    void getCategoryByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(categoryRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryUseCase.getCategoryByIdOrThrow(99L));
    }
}


