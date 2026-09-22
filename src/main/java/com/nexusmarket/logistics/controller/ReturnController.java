package com.nexusmarket.logistics.controller;

import com.nexusmarket.logistics.dto.ReturnCreateRequest;
import com.nexusmarket.logistics.dto.ReturnResponse;
import com.nexusmarket.logistics.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    public ResponseEntity<ReturnResponse> createReturn(@Valid @RequestBody ReturnCreateRequest request) {
        ReturnResponse response = returnService.createReturn(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnResponse> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.getReturnResponseById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReturnResponse>> getReturnsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(returnService.findByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<ReturnResponse>> getAllReturns() {
        return ResponseEntity.ok(returnService.findAll());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.approveReturn(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.rejectReturn(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReturnResponse> completeReturn(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.completeReturn(id));
    }
}
