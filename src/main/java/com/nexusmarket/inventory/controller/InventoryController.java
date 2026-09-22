package com.nexusmarket.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.dto.InventoryCreateRequest;
import com.nexusmarket.inventory.dto.InventoryResponse;
import com.nexusmarket.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/inventory", "/api/inventories"})
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryCreateRequest request) {
        InventoryResponse inventory = inventoryService.createInventory(request);
        return new ResponseEntity<>(inventory, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getByIdOrThrow(id);
        return ResponseEntity.ok(InventoryResponse.fromEntity(inventory));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        List<InventoryResponse> list = inventoryService.findAll().stream()
                .map(InventoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByProduct(@PathVariable Long productId) {
        List<InventoryResponse> list = inventoryService.findByProduct(productId).stream()
                .map(InventoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByWarehouse(@PathVariable Long warehouseId) {
        List<InventoryResponse> list = inventoryService.findByWarehouse(warehouseId).stream()
                .map(InventoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<InventoryResponse> reserve(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.reserve(id, amount);
        return ResponseEntity.ok(InventoryResponse.fromEntity(inventory));
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<InventoryResponse> confirmPayment(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.confirmPayment(id, amount);
        return ResponseEntity.ok(InventoryResponse.fromEntity(inventory));
    }

    @PatchMapping({"/{id}/damage", "/{id}/mark-damaged"})
    public ResponseEntity<InventoryResponse> markAsDamaged(@PathVariable Long id) {
        Inventory inventory = inventoryService.markAsDamaged(id);
        return ResponseEntity.ok(InventoryResponse.fromEntity(inventory));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryResponse> adjust(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.adjust(id, amount);
        return ResponseEntity.ok(InventoryResponse.fromEntity(inventory));
    }
}
