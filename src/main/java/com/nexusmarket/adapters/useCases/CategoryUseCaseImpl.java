package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.ports.in.CategoryUseCasePort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.services.CategoryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryUseCaseImpl implements CategoryUseCasePort {

    private final CategoryDomainService categoryDomainService;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public Category createCategory(String name, String description, Long parentId) {
        return categoryDomainService.createCategory(name, description, parentId);
    }

    @Override
    public Category getCategoryByIdOrThrow(Long id) {
        return categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Override
    public List<Category> findAll() {
        return categoryRepositoryPort.findAll();
    }

    @Override
    public List<Category> findRootCategories() {
        return categoryRepositoryPort.findByParentIsNull();
    }

    @Override
    public Category updateCategory(Long id, String name, String description, Long parentId) {
        return categoryDomainService.updateCategory(id, name, description, parentId);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryDomainService.deleteCategory(id);
    }
}
