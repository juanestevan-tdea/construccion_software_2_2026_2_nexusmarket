package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.CategoryCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.CategoryUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.CategoryResponseDTO;
import com.nexusmarket.adapters.rest.mappers.CategoryRestMapper;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.ports.in.CategoryUseCasePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCasePort categoryUseCasePort;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateRequestDTO request) {
        Category category = categoryUseCasePort.createCategory(request.getName(), request.getDescription(), request.getParentId());
        return new ResponseEntity<>(CategoryRestMapper.toResponseDTO(category), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> list = categoryUseCasePort.findAll().stream()
                .map(CategoryRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponseDTO>> getRootCategories() {
        List<CategoryResponseDTO> list = categoryUseCasePort.findRootCategories().stream()
                .map(CategoryRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(CategoryRestMapper.toResponseDTO(categoryUseCasePort.getCategoryByIdOrThrow(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequestDTO request) {
        Category updated = categoryUseCasePort.updateCategory(id, request.getName(), request.getDescription(), request.getParentId());
        return ResponseEntity.ok(CategoryRestMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryUseCasePort.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
