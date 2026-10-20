package com.nexusmarket.adapters.rest.controllers.buyer;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.OrderItemRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import com.nexusmarket.adapters.rest.mappers.OrderRestMapper;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/buyer/orders")
@PreAuthorize("hasRole('BUYER')")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCasePort orderUseCasePort;
    private final BuyerRepositoryPort buyerRepositoryPort;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal UserDetailsAdapter principal) {
        Order order = orderUseCasePort.createOrder(currentBuyer(principal).getId());
        return new ResponseEntity<>(OrderRestMapper.toResponseDTO(order), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(ownedOrder(id, principal)));
    }

    /**
     * Lista únicamente las órdenes del comprador autenticado. Antes exponía
     * {@code findAll()} y filtraba las órdenes de todos los compradores.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @AuthenticationPrincipal UserDetailsAdapter principal) {
        Long buyerId = currentBuyer(principal).getId();
        List<OrderResponseDTO> orders = orderUseCasePort.findByBuyer(buyerId).stream()
                .map(OrderRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByBuyer(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long buyerId) {

        Long authenticatedBuyerId = currentBuyer(principal).getId();
        if (!authenticatedBuyerId.equals(buyerId)) {
            throw new AccessDeniedException("Buyers can only query their own orders");
        }
        List<OrderResponseDTO> orders = orderUseCasePort.findByBuyer(buyerId).stream()
                .map(OrderRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponseDTO> addItem(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id,
            @Valid @RequestBody OrderItemRequestDTO request) {

        requireOwnership(id, principal);
        Order order = orderUseCasePort.addItem(id, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<OrderResponseDTO> removeItem(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id,
            @PathVariable Long itemId) {

        requireOwnership(id, principal);
        Order order = orderUseCasePort.removeItem(id, itemId);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {
        requireOwnership(id, principal);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(orderUseCasePort.checkout(id)));
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderResponseDTO> confirmPayment(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {
        requireOwnership(id, principal);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(orderUseCasePort.confirmPayment(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {
        requireOwnership(id, principal);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(orderUseCasePort.cancel(id)));
    }

    /** Resuelve el comprador autenticado desde el {@code userId} del token. */
    private Buyer currentBuyer(UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("No authenticated buyer found");
        }
        Long userId = principal.getUser().getId();
        return buyerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", userId));
    }

    /** Devuelve la orden solo si pertenece al comprador autenticado. */
    private Order ownedOrder(Long orderId, UserDetailsAdapter principal) {
        Order order = orderUseCasePort.getByIdOrThrow(orderId);
        if (!order.getBuyerId().equals(currentBuyer(principal).getId())) {
            throw new AccessDeniedException("Buyer does not own order " + orderId);
        }
        return order;
    }

    private void requireOwnership(Long orderId, UserDetailsAdapter principal) {
        ownedOrder(orderId, principal);
    }
}
