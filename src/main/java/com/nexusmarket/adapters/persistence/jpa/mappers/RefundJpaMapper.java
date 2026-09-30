package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.RefundJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.InvoiceJpaRepository;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundJpaMapper {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final ReturnRepository returnRepository;

    public Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Refund.builder()
                .id(entity.getId())
                .invoiceId(entity.getInvoice() != null ? entity.getInvoice().getId() : null)
                .returnRequestId(entity.getReturnRequest() != null ? entity.getReturnRequest().getId() : null)
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .build();
    }

    public RefundJpaEntity toEntity(Refund domain) {
        if (domain == null) {
            return null;
        }

        InvoiceJpaEntity invoice = null;
        if (domain.getInvoiceId() != null) {
            invoice = invoiceJpaRepository.findById(domain.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice", domain.getInvoiceId()));
        }

        Return returnRequest = null;
        if (domain.getReturnRequestId() != null) {
            returnRequest = returnRepository.findById(domain.getReturnRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Return", domain.getReturnRequestId()));
        }

        return RefundJpaEntity.builder()
                .id(domain.getId())
                .invoice(invoice)
                .returnRequest(returnRequest)
                .amount(domain.getAmount())
                .status(domain.getStatus())
                .build();
    }
}
