package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Refund;

import java.math.BigDecimal;
import java.util.List;

public interface RefundUseCasePort {

    Refund createRefund(Long invoiceId, Long returnRequestId, BigDecimal amount);

    Refund getByIdOrThrow(Long id);

    List<Refund> findByInvoice(Long invoiceId);

    List<Refund> findAll();

    Refund approveRefund(Long id);

    Refund rejectRefund(Long id);
}
