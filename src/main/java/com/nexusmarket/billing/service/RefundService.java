package com.nexusmarket.billing.service;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import com.nexusmarket.billing.domain.repository.InvoiceRepository;
import com.nexusmarket.billing.domain.repository.RefundRepository;
import com.nexusmarket.billing.dto.request.RefundCreateRequest;
import com.nexusmarket.billing.dto.response.RefundResponse;
import com.nexusmarket.common.exception.RefundAmountExceededException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReturnRepository returnRepository;

    @Transactional
    public RefundResponse createRefund(RefundCreateRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", request.getInvoiceId()));

        if (!Boolean.TRUE.equals(invoice.getPaid())) {
            throw new RefundNotAllowedException("Invoice", "paid", "false");
        }

        if (request.getAmount().compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "amount", request.getAmount());
        }

        // Suma de reembolsos activos (PENDING o APPROVED)
        List<Refund> existingRefunds = refundRepository.findByInvoice(invoice);
        BigDecimal currentRefunded = existingRefunds.stream()
                .filter(r -> r.getStatus() == RefundStatus.PENDING || r.getStatus() == RefundStatus.APPROVED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentRefunded.add(request.getAmount()).compareTo(invoice.getAmount()) > 0) {
            throw new RefundAmountExceededException("Refund", "totalRefunded",
                    String.format("Current refunds sum (%s) + new amount (%s) exceeds invoice amount (%s)",
                            currentRefunded, request.getAmount(), invoice.getAmount()));
        }

        Return returnRequest = null;
        if (request.getReturnRequestId() != null) {
            returnRequest = returnRepository.findById(request.getReturnRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Return", request.getReturnRequestId()));
        }

        Refund refund = Refund.builder()
                .invoice(invoice)
                .returnRequest(returnRequest)
                .amount(request.getAmount())
                .status(RefundStatus.PENDING)
                .build();

        return RefundResponse.fromEntity(refundRepository.save(refund));
    }

    @Transactional(readOnly = true)
    public Refund getByIdOrThrow(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundResponseById(Long id) {
        return RefundResponse.fromEntity(getByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> findByInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return refundRepository.findByInvoice(invoice).stream()
                .map(RefundResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> findAll() {
        return refundRepository.findAll().stream()
                .map(RefundResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RefundResponse approveRefund(Long id) {
        Refund refund = getByIdOrThrow(id);
        refund.setStatus(RefundStatus.APPROVED);
        return RefundResponse.fromEntity(refundRepository.save(refund));
    }

    @Transactional
    public RefundResponse rejectRefund(Long id) {
        Refund refund = getByIdOrThrow(id);
        refund.setStatus(RefundStatus.REJECTED);
        return RefundResponse.fromEntity(refundRepository.save(refund));
    }
}
