package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.CategoryResponseDTO;
import com.nexusmarket.domain.models.Category;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class CategoryRestMapper {

    public CategoryResponseDTO toResponseDTO(Category domain) {
        if (domain == null) {
            return null;
        }
        List<CategoryResponseDTO> subList = domain.getSubCategories() != null
                ? domain.getSubCategories().stream().map(CategoryRestMapper::toResponseDTOSimple).toList()
                : Collections.emptyList();

        return CategoryResponseDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .parentId(domain.getParentId())
                .parentName(domain.getParentName())
                .subCategories(subList)
                .build();
    }

    public CategoryResponseDTO toResponseDTOSimple(Category domain) {
        if (domain == null) {
            return null;
        }
        return CategoryResponseDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .parentId(domain.getParentId())
                .parentName(domain.getParentName())
                .subCategories(Collections.emptyList())
                .build();
    }
}
