package com.nexusmarket.adapters.rest.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.ProductCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.ProductUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ProductRestMapper;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.in.ProductUseCasePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCasePort productUseCasePort;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateRequestDTO request) {
        Product product = productUseCasePort.createProduct(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getType(),
                request.getSellerId(),
                request.getCategoryId()
        );
        return new ResponseEntity<>(ProductRestMapper.toResponseDTO(product), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(productUseCasePort.getProductByIdOrThrow(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productUseCasePort.findAll().stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDTO> products = productUseCasePort.findByCategory(categoryId).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsBySeller(@PathVariable Long sellerId) {
        List<ProductResponseDTO> products = productUseCasePort.findBySeller(sellerId).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPriceRange(@RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<ProductResponseDTO> products = productUseCasePort.findByPriceRange(min, max).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDTO request) {
        Product updated = productUseCasePort.updateProduct(
                id,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getType(),
                request.getCategoryId()
        );
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) {
        Product deactivated = productUseCasePort.deactivateProduct(id);
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(deactivated));
    }
}
