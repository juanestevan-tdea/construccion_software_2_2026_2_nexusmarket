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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.BuyerCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import com.nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerUseCasePort buyerUseCasePort;
    private final UserUseCasePort userUseCasePort;

    @PostMapping
    public ResponseEntity<BuyerResponseDTO> createBuyer(@Valid @RequestBody BuyerCreateRequestDTO request) {
        Buyer newBuyer = buyerUseCasePort.createBuyer(request.getUserId(), request.getPrimaryAddress());
        User user = userUseCasePort.getUserByIdOrThrow(newBuyer.getUserId());
        return new ResponseEntity<>(BuyerRestMapper.toResponseDTO(newBuyer, user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponseDTO> getBuyerById(@PathVariable Long id) {
        Buyer buyer = buyerUseCasePort.getBuyerByIdOrThrow(id);
        User user = userUseCasePort.getUserByIdOrThrow(buyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(buyer, user));
    }

    @GetMapping
    public ResponseEntity<List<BuyerResponseDTO>> getAllBuyers() {
        List<BuyerResponseDTO> buyers = buyerUseCasePort.findAll().stream()
                .map(b -> BuyerRestMapper.toResponseDTO(b, userUseCasePort.findById(b.getUserId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(buyers);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BuyerResponseDTO>> getBuyersByStatus(@PathVariable BuyerCommercialStatus status) {
        List<BuyerResponseDTO> buyers = buyerUseCasePort.findByCommercialStatus(status).stream()
                .map(b -> BuyerRestMapper.toResponseDTO(b, userUseCasePort.findById(b.getUserId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(buyers);
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<BuyerResponseDTO> addAddress(@PathVariable Long id, @RequestParam String address) {
        Buyer updatedBuyer = buyerUseCasePort.addAdditionalAddress(id, address);
        User user = userUseCasePort.getUserByIdOrThrow(updatedBuyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(updatedBuyer, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BuyerResponseDTO> changeCommercialStatus(@PathVariable Long id,
            @RequestParam BuyerCommercialStatus newStatus) {
        Buyer updatedBuyer = buyerUseCasePort.changeCommercialStatus(id, newStatus);
        User user = userUseCasePort.getUserByIdOrThrow(updatedBuyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(updatedBuyer, user));
    }
}
