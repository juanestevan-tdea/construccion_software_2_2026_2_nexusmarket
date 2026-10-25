package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.InvoiceNotPayableException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvoiceVoidService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public Invoice voidInvoice(Long id) {
        Invoice invoice = invoiceRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        // RN-010 / RF-032: solo una factura activa e impaga puede anularse.
        // El reembolso se gestiona en su propio flujo; no habilita la anulación.
        if (!Boolean.TRUE.equals(invoice.getActive())) {
            throw new InvoiceNotPayableException(
                    String.format("Invoice with id '%s' is not active", id));
        }
        if (Boolean.TRUE.equals(invoice.getPaid())) {
            throw new InvoiceNotPayableException(
                    String.format("Invoice with id '%s' is paid and cannot be voided", id));
        }

        invoice.setActive(false);
        return invoiceRepositoryPort.save(invoice);
    }
}

