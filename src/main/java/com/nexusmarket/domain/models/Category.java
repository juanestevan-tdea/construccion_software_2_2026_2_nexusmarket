package com.nexusmarket.domain.models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();
}
