package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByOrderIdAndActiveTrue(Long orderId);

    List<Invoice> findByActive(Boolean active);

    List<Invoice> findAll();
}
