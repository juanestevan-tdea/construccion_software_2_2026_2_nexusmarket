package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.InvoiceAlreadyExistsException;
import com.nexusmarket.common.exception.InvoiceNotPayableException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceGenerateService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderRepository orderRepository;

    @Transactional
    public Invoice generateInvoice(Long orderId, String pdfUrl) {
        Order order = orderRepository.findById(orderId)
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
