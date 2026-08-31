package com.nexusmarket.users.controller;

import com.nexusmarket.users.domain.model.Seller;
import com.nexusmarket.users.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // Crear un vendedor (solo Admin debería poder hacer esto)
    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestParam Long userId,
                                               @RequestParam String taxId,
                                               @RequestParam String companyName) {
        Seller newSeller = sellerService.createSeller(userId, taxId, companyName);
        return new ResponseEntity<>(newSeller, HttpStatus.CREATED);
    }

    // Obtener vendedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) {
        Optional<Seller> seller = sellerService.findById(id);
        return seller.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener vendedor por NIT/RUT
    @GetMapping("/taxId")
    public ResponseEntity<Seller> getSellerByTaxId(@RequestParam String taxId) {
        Optional<Seller> seller = sellerService.findByTaxId(taxId);
        return seller.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar todos los vendedores
    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers() {
        return ResponseEntity.ok(sellerService.findAll());
    }

    // Activar un vendedor
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Seller> activateSeller(@PathVariable Long id) {
        Seller activatedSeller = sellerService.activateSeller(id);
        return ResponseEntity.ok(activatedSeller);
    }

    // Desactivar un vendedor
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Seller> deactivateSeller(@PathVariable Long id) {
        Seller deactivatedSeller = sellerService.deactivateSeller(id);
        return ResponseEntity.ok(deactivatedSeller);
    }

    // Actualizar información de un vendedor
    @PatchMapping("/{id}/update")
    public ResponseEntity<Seller> updateSeller(@PathVariable Long id,
                                               @RequestParam(required = false) String companyName,
                                               @RequestParam(required = false) String taxId) {
        Seller updatedSeller = sellerService.updateSeller(id, companyName, taxId);
        return ResponseEntity.ok(updatedSeller);
    }
}