package com.nexusmarket.users.controller;

import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.dto.BuyerCreateRequest;
import com.nexusmarket.users.dto.BuyerResponse;
import com.nexusmarket.users.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    // Crear un comprador usando DTO y Bean Validation
    @PostMapping
    public ResponseEntity<BuyerResponse> createBuyer(@Valid @RequestBody BuyerCreateRequest request) {
        BuyerResponse newBuyer = buyerService.createBuyer(request);
        return new ResponseEntity<>(newBuyer, HttpStatus.CREATED);
    }

    // Obtener comprador por ID
    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponse> getBuyerById(@PathVariable Long id) {
        BuyerResponse buyer = BuyerResponse.fromEntity(buyerService.getBuyerByIdOrThrow(id));
        return ResponseEntity.ok(buyer);
    }

    // Listar todos los compradores
    @GetMapping
    public ResponseEntity<List<BuyerResponse>> getAllBuyers() {
        List<BuyerResponse> buyers = buyerService.findAll().stream()
                .map(BuyerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(buyers);
    }

    // Listar compradores por estado comercial
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BuyerResponse>> getBuyersByStatus(@PathVariable BuyerCommercialStatus status) {
        List<BuyerResponse> buyers = buyerService.findByCommercialStatus(status).stream()
                .map(BuyerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(buyers);
    }

    // Agregar una dirección adicional
    @PostMapping("/{id}/addresses")
    public ResponseEntity<BuyerResponse> addAddress(@PathVariable Long id, @RequestParam String address) {
        BuyerResponse updatedBuyer = BuyerResponse.fromEntity(buyerService.addAdditionalAddress(id, address));
        return ResponseEntity.ok(updatedBuyer);
    }

    // Cambiar estado comercial
    @PatchMapping("/{id}/status")
    public ResponseEntity<BuyerResponse> changeCommercialStatus(@PathVariable Long id,
                                                                @RequestParam BuyerCommercialStatus newStatus) {
        BuyerResponse updatedBuyer = BuyerResponse.fromEntity(buyerService.changeCommercialStatus(id, newStatus));
        return ResponseEntity.ok(updatedBuyer);
    }
}