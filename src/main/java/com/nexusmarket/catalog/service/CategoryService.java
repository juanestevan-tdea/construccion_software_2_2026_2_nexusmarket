package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.CategoryCreateRequest;
import com.nexusmarket.catalog.dto.CategoryResponse;
import com.nexusmarket.catalog.dto.CategoryUpdateRequest;
import com.nexusmarket.exception.CategoryHasProductsException;
import com.nexusmarket.exception.DuplicateResourceException;
import com.nexusmarket.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .build();

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public Category getCategoryByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponseById(Long id) {
        return CategoryResponse.fromEntity(getCategoryByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = getCategoryByIdOrThrow(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            if (categoryRepository.findByName(request.getName())
                    .filter(c -> !c.getId().equals(id))
                    .isPresent()) {
                throw new DuplicateResourceException("Category", "name", request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", request.getParentId()));
            category.setParent(parent);
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryByIdOrThrow(id);

        // Validar que no tenga productos asociados
        if (!productRepository.findByCategory(category).isEmpty()) {
            throw new CategoryHasProductsException("Category", "id", id);
        }

        categoryRepository.delete(category);
    }
}
