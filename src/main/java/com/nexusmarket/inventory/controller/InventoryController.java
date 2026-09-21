package com.nexusmarket.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.createInventory(productId, warehouseId, quantity);
        return new ResponseEntity<>(inventory, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getByIdOrThrow(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getInventoriesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Inventory>> getInventoriesByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.findByWarehouse(warehouseId));
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<Inventory> reserve(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.reserve(id, amount);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<Inventory> confirmPayment(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.confirmPayment(id, amount);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/{id}/mark-damaged")
    public ResponseEntity<Inventory> markAsDamaged(@PathVariable Long id) {
        Inventory inventory = inventoryService.markAsDamaged(id);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<Inventory> adjust(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryService.adjust(id, amount);
        return ResponseEntity.ok(inventory);
    }
}
