package com.nexusmarket.billing.domain.repository;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByInvoice(Invoice invoice);
    List<Refund> findByInvoiceAndStatus(Invoice invoice, RefundStatus status);
}
