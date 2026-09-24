package com.nexusmarket.billing.controller;

import com.nexusmarket.billing.dto.request.InvoiceCreateRequest;
import com.nexusmarket.billing.dto.response.InvoiceResponse;
import com.nexusmarket.billing.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> generateInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        InvoiceResponse response = invoiceService.generateInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceResponseById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.findByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.findAll());
    }

    @PatchMapping("/{id}/void")
    public ResponseEntity<InvoiceResponse> voidInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.voidInvoice(id));
    }
}
