package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.CategoryJpaEntity;
import com.nexusmarket.domain.models.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CategoryJpaMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Category> subs = entity.getSubCategories() != null
                ? entity.getSubCategories().stream().map(this::toDomainSimple).toList()
                : Collections.emptyList();

        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .subCategories(new ArrayList<>(subs))
                .build();
    }

    public Category toDomainSimple(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .subCategories(Collections.emptyList())
                .build();
    }

    public CategoryJpaEntity toEntity(Category domain, CategoryJpaEntity parentEntity) {
        if (domain == null) {
            return null;
        }
        return CategoryJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .parent(parentEntity)
                .build();
    }
}
