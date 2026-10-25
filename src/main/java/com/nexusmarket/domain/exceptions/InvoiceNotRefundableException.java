package com.nexusmarket.domain.exceptions;

/**
 * La factura no es reembolsable por su estado (inactiva o impaga), HTTP 422.
 *
 * <p>No confundir con {@link RefundAmountExceededException}, que cubre montos
 * superiores al total de la factura.</p>
 */
public class InvoiceNotRefundableException extends DomainException {

    public InvoiceNotRefundableException(String message) {
        super(message);
    }

    public InvoiceNotRefundableException(Long invoiceId, String reason) {
        super(String.format("Invoice '%s' is not refundable: %s", invoiceId, reason));
    }
}
