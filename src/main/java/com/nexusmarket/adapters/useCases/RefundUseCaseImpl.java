package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.in.RefundUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.services.RefundApproveService;
import com.nexusmarket.domain.services.RefundProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundUseCaseImpl implements RefundUseCasePort {

    private final RefundProcessService refundProcessService;
    private final RefundApproveService refundApproveService;
    private final RefundRepositoryPort refundRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    @Override
    public Refund createRefund(Long invoiceId, Long returnRequestId, BigDecimal amount) {
        return refundProcessService.createRefund(invoiceId, returnRequestId, amount);
    }

    @Override
    public Refund getByIdOrThrow(Long id) {
        return refundRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
    }

    @Override
    public List<Refund> findByInvoice(Long invoiceId) {
        if (invoiceRepositoryPort.findById(invoiceId).isEmpty()) {
            throw new ResourceNotFoundException("Invoice", invoiceId);
        }
        return refundRepositoryPort.findByInvoiceId(invoiceId);
    }

    @Override
    public List<Refund> findAll() {
        return refundRepositoryPort.findAll();
    }

    @Override
    public Refund approveRefund(Long id) {
        return refundApproveService.approveRefund(id);
    }

    @Override
    public Refund rejectRefund(Long id) {
        return refundApproveService.rejectRefund(id);
    }
}
