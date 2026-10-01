package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    boolean existsById(Long id);

    boolean existsByName(String name);

    List<Category> findAll();

    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

    void delete(Category category);
}
