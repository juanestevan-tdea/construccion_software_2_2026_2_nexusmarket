package com.nexusmarket.users.controller;

import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    // Crear un comprador a partir de un userId existente
    @PostMapping
    public ResponseEntity<Buyer> createBuyer(@RequestParam Long userId,
                                             @RequestParam String primaryAddress) {
        Buyer newBuyer = buyerService.createBuyer(userId, primaryAddress);
        return new ResponseEntity<>(newBuyer, HttpStatus.CREATED);
    }

    // Obtener comprador por ID
    @GetMapping("/{id}")
    public ResponseEntity<Buyer> getBuyerById(@PathVariable Long id) {
        Optional<Buyer> buyer = buyerService.findById(id);
        return buyer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar todos los compradores
    @GetMapping
    public ResponseEntity<List<Buyer>> getAllBuyers() {
        return ResponseEntity.ok(buyerService.findAll());
    }

    // Listar compradores por estado comercial
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Buyer>> getBuyersByStatus(@PathVariable BuyerCommercialStatus status) {
        return ResponseEntity.ok(buyerService.findByCommercialStatus(status));
    }

    // Agregar una dirección adicional
    @PostMapping("/{id}/addresses")
    public ResponseEntity<Buyer> addAddress(@PathVariable Long id, @RequestParam String address) {
        Buyer updatedBuyer = buyerService.addAdditionalAddress(id, address);
        return ResponseEntity.ok(updatedBuyer);
    }

    // Cambiar estado comercial
    @PatchMapping("/{id}/status")
    public ResponseEntity<Buyer> changeCommercialStatus(@PathVariable Long id,
                                                        @RequestParam BuyerCommercialStatus newStatus) {
        Buyer updatedBuyer = buyerService.changeCommercialStatus(id, newStatus);
        return ResponseEntity.ok(updatedBuyer);
    }
}