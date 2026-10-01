package com.nexusmarket.domain.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.common.exception.InvoiceAlreadyExistsException;
import com.nexusmarket.common.exception.InvoiceNotPayableException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.valueobjects.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceGenerateService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @Transactional
    public Invoice generateInvoice(Long orderId, String pdfUrl) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.DISPATCHED
                && order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.FINISHED) {
            throw new InvoiceNotPayableException("Order", "status", order.getStatus().name());
        }

        if (invoiceRepositoryPort.existsByOrderIdAndActiveTrue(order.getId())) {
            throw new InvoiceAlreadyExistsException("Invoice", "orderId", order.getId());
        }

        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Invoice invoice = Invoice.builder()
                .orderId(order.getId())
                .invoiceNumber(invoiceNumber)
                .issueDate(LocalDateTime.now())
                .amount(order.getTotalAmount())
                .pdfUrl(pdfUrl)
                .active(true)
                .paid(true)
                .build();

        return invoiceRepositoryPort.save(invoice);
    }
}
