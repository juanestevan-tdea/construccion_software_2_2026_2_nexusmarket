package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefundApproveService {

    private final RefundRepositoryPort refundRepositoryPort;

    public Refund approveRefund(Long id) {
        Refund refund = getPendingRefund(id, RefundStatus.APPROVED);
        refund.setStatus(RefundStatus.APPROVED);
        return refundRepositoryPort.save(refund);
    }

    public Refund rejectRefund(Long id) {
        Refund refund = getPendingRefund(id, RefundStatus.REJECTED);
        refund.setStatus(RefundStatus.REJECTED);
        return refundRepositoryPort.save(refund);
    }

    private Refund getPendingRefund(Long id, RefundStatus targetStatus) {
        Refund refund = refundRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    refund.getStatus() == null ? "UNKNOWN" : refund.getStatus().name(),
                    targetStatus.name());
        }
        return refund;
    }
}

