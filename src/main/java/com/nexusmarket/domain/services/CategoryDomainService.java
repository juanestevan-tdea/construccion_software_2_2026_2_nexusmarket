package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.CategoryHasProductsException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryDomainService {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Transactional
    public Category createCategory(String name, String description, Long parentId) {
        if (categoryRepositoryPort.existsByName(name)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        Category parent = null;
        if (parentId != null) {
            parent = categoryRepositoryPort.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", parentId));
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .parentId(parent != null ? parent.getId() : null)
                .parentName(parent != null ? parent.getName() : null)
                .build();

        return categoryRepositoryPort.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, Long parentId) {
        Category category = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (name != null && !name.isBlank()) {
            if (categoryRepositoryPort.findByName(name)
                    .filter(c -> !c.getId().equals(id))
                    .isPresent()) {
                throw new DuplicateResourceException("Category", "name", name);
            }
            category.setName(name);
        }

        if (description != null) {
            category.setDescription(description);
        }

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new BusinessRuleException("A category cannot be its own parent");
            }
            Category parent = categoryRepositoryPort.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", parentId));
            category.setParentId(parent.getId());
            category.setParentName(parent.getName());
        }

        return categoryRepositoryPort.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!productRepositoryPort.findByCategoryId(id).isEmpty()) {
            throw new CategoryHasProductsException("Category", "id", id);
        }

        categoryRepositoryPort.delete(category);
    }
}

