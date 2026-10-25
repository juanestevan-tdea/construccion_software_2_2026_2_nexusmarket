package com.nexusmarket.domain.services;

import java.math.BigDecimal;
import java.util.List;

import com.nexusmarket.domain.exceptions.InvalidRefundAmountException;
import com.nexusmarket.domain.exceptions.InvoiceNotRefundableException;
import com.nexusmarket.domain.exceptions.RefundAmountExceededException;
import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefundProcessService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;

    public Refund createRefund(Long invoiceId, Long returnRequestId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRefundAmountException(amount);
        }

        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!Boolean.TRUE.equals(invoice.getActive())) {
            throw new InvoiceNotRefundableException(invoice.getId(), "invoice is inactive");
        }

        if (!Boolean.TRUE.equals(invoice.getPaid())) {
            throw new InvoiceNotRefundableException(invoice.getId(), "invoice has not been paid");
        }

        if (amount.compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "amount", amount);
        }

        List<Refund> existingRefunds = refundRepositoryPort.findByInvoiceId(invoice.getId());
        BigDecimal currentRefunded = existingRefunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.PENDING
                        || r.getStatus() == RefundStatus.APPROVED
                        || r.getStatus() == RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentRefunded.add(amount).compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "totalRefunded",
                    String.format("Current refunds sum (%s) + new amount (%s) exceeds invoice amount (%s)",
                            currentRefunded, amount, invoice.getAmount()));
        }

        if (returnRequestId != null) {
            Return request = returnRepositoryPort.findById(returnRequestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Return", returnRequestId));

            // P2-08: la devolución debe pertenecer a la misma orden que la factura.
            if (invoice.getOrderId() == null || !invoice.getOrderId().equals(request.getOrderId())) {
                throw new RefundNotAllowedException("Return", "orderId", request.getOrderId());
            }
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

