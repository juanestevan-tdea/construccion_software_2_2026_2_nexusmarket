package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.InventoryCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.InventoryResponseDTO;
import com.nexusmarket.adapters.rest.mappers.InventoryRestMapper;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.ports.in.InventoryUseCasePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCasePort inventoryUseCasePort;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(@Valid @RequestBody InventoryCreateRequestDTO request) {
        Inventory inventory = inventoryUseCasePort.createInventory(
                request.getProductId(),
                request.getWarehouseId(),
                request.getQuantity()
        );
        return new ResponseEntity<>(InventoryRestMapper.toResponseDTO(inventory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(inventory));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories() {
        List<InventoryResponseDTO> list = inventoryUseCasePort.findAll().stream()
                .map(InventoryRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponseDTO>> getInventoriesByProduct(@PathVariable Long productId) {
        List<InventoryResponseDTO> list = inventoryUseCasePort.findByProduct(productId).stream()
                .map(InventoryRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponseDTO>> getInventoriesByWarehouse(@PathVariable Long warehouseId) {
        List<InventoryResponseDTO> list = inventoryUseCasePort.findByWarehouse(warehouseId).stream()
                .map(InventoryRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<InventoryResponseDTO> reserve(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryUseCasePort.reserve(id, amount);
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<InventoryResponseDTO> confirmPayment(@PathVariable Long id) {
        Inventory inventory = inventoryUseCasePort.confirmPayment(id);
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/{id}/mark-damaged")
    public ResponseEntity<InventoryResponseDTO> markAsDamaged(@PathVariable Long id) {
        Inventory inventory = inventoryUseCasePort.markAsDamaged(id);
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryResponseDTO> adjust(@PathVariable Long id, @RequestParam int amount) {
        Inventory inventory = inventoryUseCasePort.adjust(id, amount);
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(inventory));
    }
}
