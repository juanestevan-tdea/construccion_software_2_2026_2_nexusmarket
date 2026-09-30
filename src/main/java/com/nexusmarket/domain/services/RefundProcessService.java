package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.RefundAmountExceededException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundProcessService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ReturnRepository returnRepository;

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

        if (returnRequestId != null && !returnRepository.existsById(returnRequestId)) {
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
