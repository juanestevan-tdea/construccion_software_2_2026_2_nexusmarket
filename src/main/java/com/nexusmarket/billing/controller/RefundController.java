package com.nexusmarket.billing.controller;

import com.nexusmarket.billing.dto.RefundCreateRequest;
import com.nexusmarket.billing.dto.RefundResponse;
import com.nexusmarket.billing.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody RefundCreateRequest request) {
        RefundResponse response = refundService.createRefund(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.getRefundResponseById(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(refundService.findByInvoice(invoiceId));
    }

    @GetMapping
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        return ResponseEntity.ok(refundService.findAll());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<RefundResponse> approveRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.approveRefund(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<RefundResponse> rejectRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.rejectRefund(id));
    }
}
