package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.CategoryJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.CategoryJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.CategoryJpaRepository;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryJpaAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryJpaMapper categoryJpaMapper;

    @Override
    public Category save(Category category) {
        if (category == null) {
            return null;
        }
        CategoryJpaEntity parent = null;
        if (category.getParentId() != null) {
            parent = categoryJpaRepository.findById(category.getParentId()).orElse(null);
        }
        CategoryJpaEntity entity = categoryJpaMapper.toEntity(category, parent);
        CategoryJpaEntity saved = categoryJpaRepository.save(entity);
        return categoryJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(categoryJpaMapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryJpaRepository.findByName(name)
                .map(categoryJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryJpaRepository.existsByName(name);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(categoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByParentIsNull() {
        return categoryJpaRepository.findByParentIsNull().stream()
                .map(categoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return categoryJpaRepository.findByParentId(parentId).stream()
                .map(categoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Category category) {
        if (category != null && category.getId() != null) {
            categoryJpaRepository.deleteById(category.getId());
        }
    }
}
