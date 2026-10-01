package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.responses.CatalogOverviewResponseDTO;
import com.nexusmarket.adapters.rest.dtos.responses.CategoryResponseDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import com.nexusmarket.adapters.rest.mappers.CategoryRestMapper;
import com.nexusmarket.adapters.rest.mappers.ProductRestMapper;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.CatalogUseCasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogUseCasePort catalogUseCasePort;

    @GetMapping
    public ResponseEntity<CatalogOverviewResponseDTO> getCatalog() {
        List<ProductResponseDTO> products = catalogUseCasePort.getActiveProducts().stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();

        List<CategoryResponseDTO> categories = catalogUseCasePort.getRootCategories().stream()
                .map(CategoryRestMapper::toResponseDTO)
                .toList();

        CatalogOverviewResponseDTO response = CatalogOverviewResponseDTO.builder()
                .products(products)
                .categories(categories)
                .totalProducts(products.size())
                .totalCategories(categories.size())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam(required = false) String query) {
        List<ProductResponseDTO> products = catalogUseCasePort.searchProducts(query).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable Long id) {
        Product product = catalogUseCasePort.getProductDetail(id);
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(product));
    }
}
