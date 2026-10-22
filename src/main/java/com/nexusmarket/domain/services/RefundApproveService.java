package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundApproveService {

    private final RefundRepositoryPort refundRepositoryPort;

    @Transactional
    public Refund approveRefund(Long id) {
        Refund refund = getPendingRefund(id, RefundStatus.APPROVED);
        refund.setStatus(RefundStatus.APPROVED);
        return refundRepositoryPort.save(refund);
    }

    @Transactional
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

