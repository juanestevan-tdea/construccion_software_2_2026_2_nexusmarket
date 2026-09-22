package com.nexusmarket.catalog.controller;

import com.nexusmarket.catalog.dto.CatalogOverviewResponse;
import com.nexusmarket.catalog.dto.ProductResponse;
import com.nexusmarket.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<CatalogOverviewResponse> getCatalog() {
        return ResponseEntity.ok(catalogService.getCatalog());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(catalogService.searchProducts(query));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getProductDetail(id));
    }
}
