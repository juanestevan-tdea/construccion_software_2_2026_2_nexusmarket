package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceJpaMapper {

    private final OrderRepository orderRepository;

    public Invoice toDomain(InvoiceJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Invoice.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .invoiceNumber(entity.getInvoiceNumber())
                .issueDate(entity.getIssueDate())
                .amount(entity.getAmount())
                .pdfUrl(entity.getPdfUrl())
                .active(entity.getActive())
                .paid(entity.getPaid())
                .build();
    }

    public InvoiceJpaEntity toEntity(Invoice domain) {
        if (domain == null) {
            return null;
        }
        Order order = null;
        if (domain.getOrderId() != null) {
            order = orderRepository.findById(domain.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", domain.getOrderId()));
        }

        return InvoiceJpaEntity.builder()
                .id(domain.getId())
                .order(order)
                .invoiceNumber(domain.getInvoiceNumber())
                .issueDate(domain.getIssueDate())
                .amount(domain.getAmount())
                .pdfUrl(domain.getPdfUrl())
                .active(domain.getActive())
                .paid(domain.getPaid())
                .build();
    }
}
