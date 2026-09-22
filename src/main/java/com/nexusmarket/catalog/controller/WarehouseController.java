package com.nexusmarket.catalog.controller;

import com.nexusmarket.catalog.domain.model.WarehouseType;
import com.nexusmarket.catalog.dto.WarehouseCreateRequest;
import com.nexusmarket.catalog.dto.WarehouseResponse;
import com.nexusmarket.catalog.dto.WarehouseUpdateRequest;
import com.nexusmarket.catalog.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseCreateRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseResponseById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<WarehouseResponse>> getWarehousesByType(@PathVariable WarehouseType type) {
        return ResponseEntity.ok(warehouseService.findByType(type));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable Long id,
            @Valid @RequestBody WarehouseUpdateRequest request) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }
}
