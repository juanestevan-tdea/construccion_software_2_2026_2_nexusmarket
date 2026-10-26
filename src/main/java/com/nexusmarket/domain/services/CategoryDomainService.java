package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.CategoryHasProductsException;
import com.nexusmarket.domain.exceptions.CategoryNameAlreadyExistsException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class CategoryDomainService {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    public Category createCategory(String name, String description, Long parentId) {
        if (categoryRepositoryPort.existsByName(name)) {
            throw new CategoryNameAlreadyExistsException(name);
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

    public Category updateCategory(Long id, String name, String description, Long parentId) {
        Category category = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (name != null && !name.isBlank()) {
            if (categoryRepositoryPort.findByName(name)
                    .filter(c -> !c.getId().equals(id))
                    .isPresent()) {
                throw new CategoryNameAlreadyExistsException(name);
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
            ensureNoCycle(id, parent);
            category.setParentId(parent.getId());
            category.setParentName(parent.getName());
        }

        return categoryRepositoryPort.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!productRepositoryPort.findByCategoryId(id).isEmpty()) {
            throw new CategoryHasProductsException("Category", "id", id);
        }

        if (!categoryRepositoryPort.findByParentId(id).isEmpty()) {
            throw new BusinessRuleException("Cannot delete a category that has subcategories");
        }

        categoryRepositoryPort.delete(category);
    }

    private void ensureNoCycle(Long categoryId, Category parent) {
        Set<Long> visited = new HashSet<>();
        Category current = parent;

        while (current != null) {
            if (categoryId.equals(current.getId())) {
                throw new BusinessRuleException("Category hierarchy cannot contain cycles");
            }
            if (current.getId() != null && !visited.add(current.getId())) {
                throw new BusinessRuleException("Category hierarchy already contains a cycle");
            }

            Long nextParentId = current.getParentId();
            if (nextParentId == null) {
                return;
            }
            current = categoryRepositoryPort.findById(nextParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", nextParentId));
        }
    }
}

