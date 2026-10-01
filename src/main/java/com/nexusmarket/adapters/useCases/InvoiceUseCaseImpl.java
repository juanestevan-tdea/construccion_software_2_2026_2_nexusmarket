package com.nexusmarket.adapters.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.in.InvoiceUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.services.InvoiceGenerateService;
import com.nexusmarket.domain.services.InvoiceVoidService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceUseCaseImpl implements InvoiceUseCasePort {

    private final InvoiceGenerateService invoiceGenerateService;
    private final InvoiceVoidService invoiceVoidService;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public Invoice generateInvoice(Long orderId, String pdfUrl) {
        return invoiceGenerateService.generateInvoice(orderId, pdfUrl);
    }

    @Override
    public Invoice getByIdOrThrow(Long id) {
        return invoiceRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Override
    public Invoice findByOrder(Long orderId) {
        if (!orderRepositoryPort.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return invoiceRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice for order", orderId));
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepositoryPort.findAll();
    }

    @Override
    public Invoice voidInvoice(Long id) {
        return invoiceVoidService.voidInvoice(id);
    }
}

