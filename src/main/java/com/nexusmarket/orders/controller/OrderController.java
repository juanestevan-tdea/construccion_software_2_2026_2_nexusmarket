package com.nexusmarket.orders.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam Long buyerId) {
        Order order = orderService.createOrder(buyerId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getByIdOrThrow(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Order>> getOrdersByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(orderService.findByBuyer(buyerId));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItem(@PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Order order = orderService.addItem(id, productId, quantity);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/confirm-payment")
    public ResponseEntity<Order> confirmPayment(@PathVariable Long id) {
        Order order = orderService.confirmPayment(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<Order> dispatch(@PathVariable Long id) {
        Order order = orderService.dispatch(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<Order> deliver(@PathVariable Long id) {
        Order order = orderService.deliver(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<Order> finish(@PathVariable Long id) {
        Order order = orderService.finish(id);
        return ResponseEntity.ok(order);
    }
}
