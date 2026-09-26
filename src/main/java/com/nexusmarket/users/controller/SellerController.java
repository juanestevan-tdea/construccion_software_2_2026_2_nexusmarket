package com.nexusmarket.users.controller;

import com.nexusmarket.users.dto.request.SellerCreateRequest;
import com.nexusmarket.users.dto.request.SellerUpdateRequest;
import com.nexusmarket.users.dto.response.SellerResponse;
import com.nexusmarket.users.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // Crear un vendedor usando DTO y Bean Validation
    @PostMapping
    public ResponseEntity<SellerResponse> createSeller(@Valid @RequestBody SellerCreateRequest request) {
        SellerResponse newSeller = sellerService.createSeller(request);
        return new ResponseEntity<>(newSeller, HttpStatus.CREATED);
    }

    // Obtener vendedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<SellerResponse> getSellerById(@PathVariable Long id) {
        SellerResponse seller = SellerResponse.fromEntity(sellerService.getSellerByIdOrThrow(id));
        return ResponseEntity.ok(seller);
    }

    // Obtener vendedor por NIT/RUT
    @GetMapping("/taxId")
    public ResponseEntity<SellerResponse> getSellerByTaxId(@RequestParam String taxId) {
        SellerResponse seller = SellerResponse.fromEntity(sellerService.getSellerByTaxIdOrThrow(taxId));
        return ResponseEntity.ok(seller);
    }

    // Listar todos los vendedores
    @GetMapping
    public ResponseEntity<List<SellerResponse>> getAllSellers() {
        List<SellerResponse> sellers = sellerService.findAll().stream()
                .map(SellerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(sellers);
    }

    // Activar un vendedor
    @PatchMapping("/{id}/activate")
    public ResponseEntity<SellerResponse> activateSeller(@PathVariable Long id) {
        SellerResponse activatedSeller = SellerResponse.fromEntity(sellerService.activateSeller(id));
        return ResponseEntity.ok(activatedSeller);
    }

    // Desactivar un vendedor
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SellerResponse> deactivateSeller(@PathVariable Long id) {
        SellerResponse deactivatedSeller = SellerResponse.fromEntity(sellerService.deactivateSeller(id));
        return ResponseEntity.ok(deactivatedSeller);
    }

    // Actualizar información de un vendedor
    @PatchMapping("/{id}/update")
    public ResponseEntity<SellerResponse> updateSeller(@PathVariable Long id,
                                                       @Valid @RequestBody SellerUpdateRequest request) {
        SellerResponse updatedSeller = SellerResponse.fromEntity(sellerService.updateSeller(id, request));
        return ResponseEntity.ok(updatedSeller);
    }
}