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
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.ReturnCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ReturnResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.buyer.ReturnUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Devoluciones del comprador autenticado.
 *
 * <p>Las transiciones de aprobación, rechazo y finalización se movieron a
 * {@code /api/v1/supervisor/returns}: un BUYER no debe aprobar su propia
 * devolución.</p>
 */
@RestController
@RequestMapping("/api/v1/buyer/returns")
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnUseCasePort returnUseCasePort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @PostMapping
    public ResponseEntity<ReturnResponseDTO> createReturn(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @Valid @RequestBody ReturnCreateRequestDTO request) {

        Long buyerId = currentBuyer(principal).getId();
        requireOrderOwnership(request.getOrderId(), buyerId);
        Return returnEntity = returnUseCasePort.createReturn(request.getOrderId(), request.getReason());
        return new ResponseEntity<>(ReturnRestMapper.toResponseDTO(returnEntity), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnResponseDTO> getReturnById(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        Long buyerId = currentBuyer(principal).getId();
        Return returnEntity = returnUseCasePort.getByIdOrThrow(id);
        requireOrderOwnership(returnEntity.getOrderId(), buyerId);
        return ResponseEntity.ok(ReturnRestMapper.toResponseDTO(returnEntity));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReturnResponseDTO>> getReturnsByOrder(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long orderId) {

        Long buyerId = currentBuyer(principal).getId();
        requireOrderOwnership(orderId, buyerId);
        List<ReturnResponseDTO> list = returnUseCasePort.findByOrder(orderId).stream()
                .map(ReturnRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    /** Devuelve solo las devoluciones de las órdenes del comprador autenticado. */
    @GetMapping
    public ResponseEntity<List<ReturnResponseDTO>> getAllReturns(
            @AuthenticationPrincipal UserDetailsAdapter principal) {

        Long buyerId = currentBuyer(principal).getId();
        List<ReturnResponseDTO> list = orderRepositoryPort.findByBuyerId(buyerId).stream()
                .flatMap(order -> returnUseCasePort.findByOrder(order.getId()).stream())
                .map(ReturnRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    private Buyer currentBuyer(UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("No authenticated buyer found");
        }
        Long userId = principal.getUser().getId();
        return buyerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", userId));
    }

    private void requireOrderOwnership(Long orderId, Long buyerId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getBuyerId().equals(buyerId)) {
            throw new AccessDeniedException("Buyers can only access their own returns");
        }
    }
}
