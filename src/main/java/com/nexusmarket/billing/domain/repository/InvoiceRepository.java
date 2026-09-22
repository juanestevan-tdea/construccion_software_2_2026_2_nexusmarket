package com.nexusmarket.billing.domain.repository;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrder(Order order);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByOrderAndActiveTrue(Order order);
    List<Invoice> findByActive(Boolean active);
}