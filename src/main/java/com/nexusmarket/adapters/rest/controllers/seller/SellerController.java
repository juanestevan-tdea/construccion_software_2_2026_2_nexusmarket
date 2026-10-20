package com.nexusmarket.adapters.rest.controllers.seller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.SellerCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.SellerUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.SellerResponseDTO;
import com.nexusmarket.adapters.rest.mappers.SellerRestMapper;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Perfil del vendedor. Las operaciones se resuelven contra el usuario
 * autenticado; un vendedor no puede leer ni modificar el perfil de otro.
 */
@RestController
@RequestMapping("/api/v1/seller/sellers")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerController {

    private final SellerUseCasePort sellerUseCasePort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final UserUseCasePort userUseCasePort;

    @PostMapping
    public ResponseEntity<SellerResponseDTO> createSeller(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @Valid @RequestBody SellerCreateRequestDTO request) {

        Long userId = authenticatedUserId(principal);
        Seller newSeller = sellerUseCasePort.createSeller(userId, request.getTaxId(), request.getCompanyName());
        User user = userUseCasePort.getUserByIdOrThrow(newSeller.getUserId());
        return new ResponseEntity<>(SellerRestMapper.toResponseDTO(newSeller, user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerResponseDTO> getSellerById(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        Seller seller = requireSelf(id, principal);
        User user = userUseCasePort.getUserByIdOrThrow(seller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(seller, user));
    }

    @GetMapping("/taxId")
    public ResponseEntity<SellerResponseDTO> getSellerByTaxId(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @RequestParam String taxId) {

        Seller seller = currentSeller(principal);
        if (!seller.getTaxId().equals(taxId)) {
            throw new AccessDeniedException("Sellers can only access their own profile");
        }
        User user = userUseCasePort.getUserByIdOrThrow(seller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(seller, user));
    }

    /** Devuelve únicamente el perfil del vendedor autenticado. */
    @GetMapping
    public ResponseEntity<List<SellerResponseDTO>> getAllSellers(
            @AuthenticationPrincipal UserDetailsAdapter principal) {

        Seller seller = currentSeller(principal);
        User user = userUseCasePort.getUserByIdOrThrow(seller.getUserId());
        return ResponseEntity.ok(List.of(SellerRestMapper.toResponseDTO(seller, user)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SellerResponseDTO> activateSeller(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        requireSelf(id, principal);
        Seller activatedSeller = sellerUseCasePort.activateSeller(id);
        User user = userUseCasePort.getUserByIdOrThrow(activatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(activatedSeller, user));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SellerResponseDTO> deactivateSeller(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        requireSelf(id, principal);
        Seller deactivatedSeller = sellerUseCasePort.deactivateSeller(id);
        User user = userUseCasePort.getUserByIdOrThrow(deactivatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(deactivatedSeller, user));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<SellerResponseDTO> updateSeller(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id,
            @Valid @RequestBody SellerUpdateRequestDTO request) {

        requireSelf(id, principal);
        Seller updatedSeller = sellerUseCasePort.updateSeller(id, request.getCompanyName(), request.getTaxId());
        User user = userUseCasePort.getUserByIdOrThrow(updatedSeller.getUserId());
        return ResponseEntity.ok(SellerRestMapper.toResponseDTO(updatedSeller, user));
    }

    private Long authenticatedUserId(UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("No authenticated seller found");
        }
        return principal.getUser().getId();
    }

    private Seller currentSeller(UserDetailsAdapter principal) {
        Long userId = authenticatedUserId(principal);
        return sellerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", userId));
    }

    private Seller requireSelf(Long sellerId, UserDetailsAdapter principal) {
        Seller seller = currentSeller(principal);
        if (!seller.getId().equals(sellerId)) {
            throw new AccessDeniedException("Sellers can only access their own profile");
        }
        return seller;
    }
}
