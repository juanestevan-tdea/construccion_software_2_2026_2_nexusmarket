package com.nexusmarket.adapters.rest.controllers.supervisor;

import com.nexusmarket.adapters.rest.dtos.requests.InvoiceCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import com.nexusmarket.adapters.rest.mappers.InvoiceRestMapper;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.in.supervisor.InvoiceUseCasePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supervisor/invoices")
@PreAuthorize("hasRole('SUPERVISOR')")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUseCasePort invoiceUseCasePort;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> generateInvoice(@Valid @RequestBody InvoiceCreateRequestDTO request) {
        Invoice invoice = invoiceUseCasePort.generateInvoice(request.getOrderId(), request.getPdfUrl());
        return new ResponseEntity<>(InvoiceRestMapper.toResponseDTO(invoice), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(InvoiceRestMapper.toResponseDTO(invoice));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceByOrder(@PathVariable Long orderId) {
        Invoice invoice = invoiceUseCasePort.findByOrder(orderId);
        return ResponseEntity.ok(InvoiceRestMapper.toResponseDTO(invoice));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<InvoiceResponseDTO> list = invoiceUseCasePort.findAll().stream()
                .map(InvoiceRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/void")
    public ResponseEntity<InvoiceResponseDTO> voidInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceUseCasePort.voidInvoice(id);
        return ResponseEntity.ok(InvoiceRestMapper.toResponseDTO(invoice));
    }
}
