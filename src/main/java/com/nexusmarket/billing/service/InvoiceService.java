package com.nexusmarket.billing.service;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import com.nexusmarket.billing.domain.repository.InvoiceRepository;
import com.nexusmarket.billing.domain.repository.RefundRepository;
import com.nexusmarket.billing.dto.InvoiceCreateRequest;
import com.nexusmarket.billing.dto.InvoiceResponse;
import com.nexusmarket.exception.InvoiceAlreadyExistsException;
import com.nexusmarket.exception.InvoiceNotPayableException;
import com.nexusmarket.exception.RefundNotAllowedException;
import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    @Transactional
    public InvoiceResponse generateInvoice(InvoiceCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        // Solo facturar órdenes en estado PAID o posterior
        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.DISPATCHED
                && order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.FINISHED) {
            throw new InvoiceNotPayableException("Order", "status", order.getStatus().name());
        }

        // Una orden no puede tener más de una factura activa
        if (invoiceRepository.existsByOrderAndActiveTrue(order)) {
            throw new InvoiceAlreadyExistsException("Invoice", "orderId", order.getId());
        }

        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber(invoiceNumber)
                .issueDate(LocalDateTime.now())
                .amount(order.getTotalAmount())
                .pdfUrl(request.getPdfUrl())
                .active(true)
                .paid(true)
                .build();

        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public Invoice getByIdOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceResponseById(Long id) {
        return InvoiceResponse.fromEntity(getByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Invoice invoice = invoiceRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice for order", orderId));
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public InvoiceResponse voidInvoice(Long id) {
        Invoice invoice = getByIdOrThrow(id);

        if (Boolean.TRUE.equals(invoice.getPaid())) {
            // Validar si tiene reembolso aprobado o procesado antes de anular factura pagada
            List<Refund> refunds = refundRepository.findByInvoiceAndStatus(invoice, RefundStatus.APPROVED);
            if (refunds.isEmpty()) {
                throw new RefundNotAllowedException("Invoice", "id",
                        "Cannot void a paid invoice without an approved refund");
            }
        }

        invoice.setActive(false);
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }
}
