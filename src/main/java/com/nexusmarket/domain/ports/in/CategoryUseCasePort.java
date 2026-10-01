package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Category;

import java.util.List;

public interface CategoryUseCasePort {

    Category createCategory(String name, String description, Long parentId);

    Category getCategoryByIdOrThrow(Long id);

    List<Category> findAll();

    List<Category> findRootCategories();

    Category updateCategory(Long id, String name, String description, Long parentId);

    void deleteCategory(Long id);
}
