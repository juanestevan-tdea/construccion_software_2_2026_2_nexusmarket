package com.nexusmarket.domain.exceptions;

public class InvoiceNotPayableException extends DomainException {

    public InvoiceNotPayableException(String message) {
        super(message);
    }

    public InvoiceNotPayableException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Invoice for %s with %s '%s' is not in a billable or voidable state",
                resourceName, fieldName, fieldValue));
    }
}
