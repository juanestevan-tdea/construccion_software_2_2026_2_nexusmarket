package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.SellerCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.SellerUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import com.nexusmarket.adapters.rest.mappers.SellerRestMapper;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.SellerUseCasePort;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
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

    private final SellerUseCasePort sellerUseCasePort;
    private final UserUseCasePort userUseCasePort;

    @PostMapping
    public ResponseEntity<SellerResponseDTO> createSeller(@Valid @RequestBody SellerCreateRequestDTO request) {
        Seller newSeller = sellerUseCasePort.createSeller(request.getUserId(), request.getTaxId(), request.getCompanyName());
        User user = userUseCasePort.getUserByIdOrThrow(newSeller.getUserId());
        return new ResponseEntity<>(SellerRestMapper.toResponseDTO(newSeller, user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerResponseDTO> getSellerById(@PathVariable Long id) {
        Seller seller = sellerUseCasePort.getSellerByIdOrThrow(id);
        User user = userUseCasePort.getUserByIdOrThrow(seller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(seller, user));
    }

    @GetMapping("/taxId")
    public ResponseEntity<SellerResponseDTO> getSellerByTaxId(@RequestParam String taxId) {
        Seller seller = sellerUseCasePort.getSellerByTaxIdOrThrow(taxId);
        User user = userUseCasePort.getUserByIdOrThrow(seller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(seller, user));
    }

    @GetMapping
    public ResponseEntity<List<SellerResponseDTO>> getAllSellers() {
        List<SellerResponseDTO> sellers = sellerUseCasePort.findAll().stream()
                .map(s -> SellerRestMapper.toResponseDTO(s, userUseCasePort.findById(s.getUserId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SellerResponseDTO> activateSeller(@PathVariable Long id) {
        Seller activatedSeller = sellerUseCasePort.activateSeller(id);
        User user = userUseCasePort.getUserByIdOrThrow(activatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(activatedSeller, user));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SellerResponseDTO> deactivateSeller(@PathVariable Long id) {
        Seller deactivatedSeller = sellerUseCasePort.deactivateSeller(id);
        User user = userUseCasePort.getUserByIdOrThrow(deactivatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(deactivatedSeller, user));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<SellerResponseDTO> updateSeller(@PathVariable Long id,
            @Valid @RequestBody SellerUpdateRequestDTO request) {
        Seller updatedSeller = sellerUseCasePort.updateSeller(id, request.getCompanyName(), request.getTaxId());
        User user = userUseCasePort.getUserByIdOrThrow(updatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(updatedSeller, user));
    }
}
