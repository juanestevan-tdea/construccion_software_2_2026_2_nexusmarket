package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.RefundCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import com.nexusmarket.adapters.rest.mappers.RefundRestMapper;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.in.RefundUseCasePort;
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

    private final RefundUseCasePort refundUseCasePort;

    @PostMapping
    public ResponseEntity<RefundResponseDTO> createRefund(@Valid @RequestBody RefundCreateRequestDTO request) {
        Refund refund = refundUseCasePort.createRefund(
                request.getInvoiceId(),
                request.getReturnRequestId(),
                request.getAmount()
        );
        return new ResponseEntity<>(RefundRestMapper.toResponseDTO(refund), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponseDTO> getRefundById(@PathVariable Long id) {
        Refund refund = refundUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(RefundRestMapper.toResponseDTO(refund));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<RefundResponseDTO>> getRefundsByInvoice(@PathVariable Long invoiceId) {
        List<RefundResponseDTO> list = refundUseCasePort.findByInvoice(invoiceId).stream()
                .map(RefundRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<RefundResponseDTO>> getAllRefunds() {
        List<RefundResponseDTO> list = refundUseCasePort.findAll().stream()
                .map(RefundRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<RefundResponseDTO> approveRefund(@PathVariable Long id) {
        Refund refund = refundUseCasePort.approveRefund(id);
        return ResponseEntity.ok(RefundRestMapper.toResponseDTO(refund));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<RefundResponseDTO> rejectRefund(@PathVariable Long id) {
        Refund refund = refundUseCasePort.rejectRefund(id);
        return ResponseEntity.ok(RefundRestMapper.toResponseDTO(refund));
    }
}
