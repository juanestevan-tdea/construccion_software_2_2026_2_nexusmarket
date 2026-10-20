package com.nexusmarket.adapters.rest.controllers.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import com.nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;

import lombok.RequiredArgsConstructor;

/**
 * Gestión administrativa del perfil comercial de los compradores.
 *
 * <p>El cambio de estado comercial estaba en
 * {@code PATCH /api/v1/buyer/buyers/{id}/status}, lo que permitía a un
 * comprador bloqueado reactivarse a sí mismo. Ahora solo un ADMIN puede
 * ejecutarlo.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/buyers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BuyerManagementController {

    private final BuyerUseCasePort buyerUseCasePort;
    private final UserUseCasePort userUseCasePort;

    @PatchMapping("/{id}/status")
    public ResponseEntity<BuyerResponseDTO> changeCommercialStatus(
            @PathVariable Long id,
            @RequestParam BuyerCommercialStatus newStatus) {

        Buyer updatedBuyer = buyerUseCasePort.changeCommercialStatus(id, newStatus);
        User user = userUseCasePort.getUserByIdOrThrow(updatedBuyer.getUserId());
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(updatedBuyer, user));
    }
}
