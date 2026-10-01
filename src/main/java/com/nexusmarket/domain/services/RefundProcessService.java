package com.nexusmarket.domain.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.domain.exceptions.RefundAmountExceededException;
import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundProcessService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;

    @Transactional
    public Refund createRefund(Long invoiceId, Long returnRequestId, BigDecimal amount) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!Boolean.TRUE.equals(invoice.getPaid())) {
            throw new RefundNotAllowedException("Invoice", "paid", "false");
        }

        if (amount.compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "amount", amount);
        }

        List<Refund> existingRefunds = refundRepositoryPort.findByInvoiceId(invoice.getId());
        BigDecimal currentRefunded = existingRefunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.PENDING || r.getStatus() == RefundStatus.APPROVED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentRefunded.add(amount).compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "totalRefunded",
                    String.format("Current refunds sum (%s) + new amount (%s) exceeds invoice amount (%s)",
                            currentRefunded, amount, invoice.getAmount()));
        }

        if (returnRequestId != null && !returnRepositoryPort.existsById(returnRequestId)) {
            throw new ResourceNotFoundException("Return", returnRequestId);
        }

        Refund refund = Refund.builder()
                .invoiceId(invoice.getId())
                .returnRequestId(returnRequestId)
                .amount(amount)
                .status(RefundStatus.PENDING)
                .build();

        return refundRepositoryPort.save(refund);
    }
}

