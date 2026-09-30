package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Invoice;

import java.util.List;

public interface InvoiceUseCasePort {

    Invoice generateInvoice(Long orderId, String pdfUrl);

    Invoice getByIdOrThrow(Long id);

    Invoice findByOrder(Long orderId);

    List<Invoice> findAll();

    Invoice voidInvoice(Long id);
}
