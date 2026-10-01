package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceVoidService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;

    @Transactional
    public Invoice voidInvoice(Long id) {
        Invoice invoice = invoiceRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (Boolean.TRUE.equals(invoice.getPaid())) {
            List<Refund> refunds = refundRepositoryPort.findByInvoiceIdAndStatus(invoice.getId(), RefundStatus.APPROVED);
            if (refunds.isEmpty()) {
                throw new RefundNotAllowedException("Invoice", "id",
                        "Cannot void a paid invoice without an approved refund");
            }
        }

        invoice.setActive(false);
        return invoiceRepositoryPort.save(invoice);
    }
}

