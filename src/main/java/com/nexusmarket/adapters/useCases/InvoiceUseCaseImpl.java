package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.in.InvoiceUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.services.InvoiceGenerateService;
import com.nexusmarket.domain.services.InvoiceVoidService;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceUseCaseImpl implements InvoiceUseCasePort {

    private final InvoiceGenerateService invoiceGenerateService;
    private final InvoiceVoidService invoiceVoidService;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderRepository orderRepository;

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
        if (!orderRepository.existsById(orderId)) {
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
