package com.nexusmarket.orders.controller;

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

import com.nexusmarket.orders.dto.request.OrderCreateRequest;
import com.nexusmarket.orders.dto.request.OrderItemRequest;
import com.nexusmarket.orders.dto.response.OrderResponse;
import com.nexusmarket.orders.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.getByIdOrThrow(id));
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByBuyer(@PathVariable Long buyerId) {
        List<OrderResponse> orders = orderService.findByBuyer(buyerId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponse> addItem(@PathVariable Long id,
                                                 @Valid @RequestBody OrderItemRequest request) {
        OrderResponse order = orderService.addItem(id, request);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<OrderResponse> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        OrderResponse order = orderService.removeItem(id, itemId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/checkout")
    public ResponseEntity<OrderResponse> checkout(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.checkout(id));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderResponse> confirmPayment(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.confirmPayment(id));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<OrderResponse> dispatch(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.dispatch(id));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliver(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.deliver(id));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<OrderResponse> finish(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.finish(id));
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        OrderResponse order = OrderResponse.fromEntity(orderService.cancel(id));
        return ResponseEntity.ok(order);
    }
}
