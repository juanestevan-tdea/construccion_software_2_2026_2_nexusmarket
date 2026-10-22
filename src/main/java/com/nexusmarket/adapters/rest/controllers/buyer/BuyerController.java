package com.nexusmarket.adapters.rest.controllers.buyer;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.BuyerCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import com.nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Perfil del comprador. Todas las operaciones se resuelven contra el usuario
 * autenticado: el {@code userId} ya no se acepta desde el cuerpo y ningún
 * comprador puede consultar o modificar el perfil de otro.
 */
@RestController
@RequestMapping("/api/v1/buyer/buyers")
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerUseCasePort buyerUseCasePort;
    private final UserUseCasePort userUseCasePort;

    @PostMapping
    public ResponseEntity<BuyerResponseDTO> createBuyer(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @Valid @RequestBody BuyerCreateRequestDTO request) {

        Long userId = authenticatedUserId(principal);
        Buyer newBuyer = buyerUseCasePort.createBuyer(userId, request.getPrimaryAddress());
        User user = userUseCasePort.getUserByIdOrThrow(newBuyer.getUserId());
        return new ResponseEntity<>(BuyerRestMapper.toResponseDTO(newBuyer, user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponseDTO> getBuyerById(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        Buyer buyer = requireSelf(id, principal);
        User user = userUseCasePort.getUserByIdOrThrow(buyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(buyer, user));
    }

    /** Devuelve únicamente el perfil del comprador autenticado. */
    @GetMapping
    public ResponseEntity<List<BuyerResponseDTO>> getAllBuyers(
            @AuthenticationPrincipal UserDetailsAdapter principal) {

        Buyer buyer = currentBuyer(principal);
        User user = userUseCasePort.getUserByIdOrThrow(buyer.getUserId());
        return ResponseEntity.ok(List.of(BuyerRestMapper.toResponseDTO(buyer, user)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BuyerResponseDTO>> getBuyersByStatus(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable BuyerCommercialStatus status) {

        Buyer buyer = currentBuyer(principal);
        if (buyer.getCommercialStatus() != status) {
            return ResponseEntity.ok(List.of());
        }
        User user = userUseCasePort.getUserByIdOrThrow(buyer.getUserId());
        return ResponseEntity.ok(List.of(BuyerRestMapper.toResponseDTO(buyer, user)));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<BuyerResponseDTO> addAddress(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id,
            @RequestParam String address) {

        requireSelf(id, principal);
        Buyer updatedBuyer = buyerUseCasePort.addAdditionalAddress(id, address);
        User user = userUseCasePort.getUserByIdOrThrow(updatedBuyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(updatedBuyer, user));
    }

    private Long authenticatedUserId(UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("No authenticated buyer found");
        }
        return principal.getUser().getId();
    }

    private Buyer currentBuyer(UserDetailsAdapter principal) {
        Long userId = authenticatedUserId(principal);
        return buyerUseCasePort.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", userId));
    }

    private Buyer requireSelf(Long buyerId, UserDetailsAdapter principal) {
        Buyer buyer = currentBuyer(principal);
        if (!buyer.getId().equals(buyerId)) {
            throw new AccessDeniedException("Buyers can only access their own profile");
        }
        return buyer;
    }
}
