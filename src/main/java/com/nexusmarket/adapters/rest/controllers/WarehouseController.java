package com.nexusmarket.adapters.rest.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.WarehouseCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.WarehouseUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.WarehouseResponseDTO;
import com.nexusmarket.adapters.rest.mappers.WarehouseRestMapper;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.in.WarehouseUseCasePort;
import com.nexusmarket.domain.valueobjects.WarehouseType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseUseCasePort warehouseUseCasePort;

    @PostMapping
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody WarehouseCreateRequestDTO request) {
        Warehouse warehouse = warehouseUseCasePort.createWarehouse(
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getCapacity()
        );
        return new ResponseEntity<>(WarehouseRestMapper.toResponseDTO(warehouse), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        List<WarehouseResponseDTO> list = warehouseUseCasePort.findAll().stream()
                .map(WarehouseRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(WarehouseRestMapper.toResponseDTO(warehouseUseCasePort.getWarehouseByIdOrThrow(id)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<WarehouseResponseDTO>> getWarehousesByType(@PathVariable WarehouseType type) {
        List<WarehouseResponseDTO> list = warehouseUseCasePort.findByType(type).stream()
                .map(WarehouseRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(@PathVariable Long id,
            @Valid @RequestBody WarehouseUpdateRequestDTO request) {
        Warehouse updated = warehouseUseCasePort.updateWarehouse(
                id,
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getCapacity()
        );
        return ResponseEntity.ok(WarehouseRestMapper.toResponseDTO(updated));
    }
}
