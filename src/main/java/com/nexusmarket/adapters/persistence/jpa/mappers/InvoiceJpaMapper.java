package com.nexusmarket.adapters.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.OrderJpaRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceJpaMapper {

    private final OrderJpaRepository orderJpaRepository;

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
        OrderJpaEntity order = null;
        if (domain.getOrderId() != null) {
            order = orderJpaRepository.findById(domain.getOrderId())
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
