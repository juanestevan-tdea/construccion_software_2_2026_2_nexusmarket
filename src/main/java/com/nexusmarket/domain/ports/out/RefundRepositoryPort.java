package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.valueobjects.RefundStatus;

import java.util.List;
import java.util.Optional;

public interface RefundRepositoryPort {

    Refund save(Refund refund);

    Optional<Refund> findById(Long id);

    List<Refund> findByInvoiceId(Long invoiceId);

    List<Refund> findByInvoiceIdAndStatus(Long invoiceId, RefundStatus status);

    List<Refund> findAll();
}
