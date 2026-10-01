package com.nexusmarket.domain.services;

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
        Refund refund = refundRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
        refund.setStatus(RefundStatus.APPROVED);
        return refundRepositoryPort.save(refund);
    }

    @Transactional
    public Refund rejectRefund(Long id) {
        Refund refund = refundRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
        refund.setStatus(RefundStatus.REJECTED);
        return refundRepositoryPort.save(refund);
    }
}

