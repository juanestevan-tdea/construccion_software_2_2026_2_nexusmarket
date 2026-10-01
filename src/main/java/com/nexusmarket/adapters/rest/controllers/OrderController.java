package com.nexusmarket.adapters.rest.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.OrderCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.OrderItemRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import com.nexusmarket.adapters.rest.mappers.OrderRestMapper;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.OrderUseCasePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCasePort orderUseCasePort;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateRequestDTO request) {
        Order order = orderUseCasePort.createOrder(request.getBuyerId());
        return new ResponseEntity<>(OrderRestMapper.toResponseDTO(order), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        Order order = orderUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderUseCasePort.findAll().stream()
                .map(OrderRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByBuyer(@PathVariable Long buyerId) {
        List<OrderResponseDTO> orders = orderUseCasePort.findByBuyer(buyerId).stream()
                .map(OrderRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponseDTO> addItem(@PathVariable Long id,
            @Valid @RequestBody OrderItemRequestDTO request) {
        Order order = orderUseCasePort.addItem(id, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<OrderResponseDTO> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        Order order = orderUseCasePort.removeItem(id, itemId);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(@PathVariable Long id) {
        Order order = orderUseCasePort.checkout(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderResponseDTO> confirmPayment(@PathVariable Long id) {
        Order order = orderUseCasePort.confirmPayment(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<OrderResponseDTO> dispatch(@PathVariable Long id) {
        Order order = orderUseCasePort.dispatch(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponseDTO> deliver(@PathVariable Long id) {
        Order order = orderUseCasePort.deliver(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<OrderResponseDTO> finish(@PathVariable Long id) {
        Order order = orderUseCasePort.finish(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id) {
        Order order = orderUseCasePort.cancel(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }
}
