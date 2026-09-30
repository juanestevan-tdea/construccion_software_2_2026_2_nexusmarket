package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.RefundJpaEntity;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, Long> {

    List<RefundJpaEntity> findByInvoice(InvoiceJpaEntity invoice);

    List<RefundJpaEntity> findByInvoiceAndStatus(InvoiceJpaEntity invoice, RefundStatus status);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.invoice.id = :invoiceId")
    List<RefundJpaEntity> findByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.invoice.id = :invoiceId AND r.status = :status")
    List<RefundJpaEntity> findByInvoiceIdAndStatus(@Param("invoiceId") Long invoiceId, @Param("status") RefundStatus status);
}
