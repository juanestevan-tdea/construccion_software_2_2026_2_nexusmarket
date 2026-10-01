package com.nexusmarket.domain.exceptions;

public class InvoiceNotPayableException extends RuntimeException {

    public InvoiceNotPayableException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Cannot generate invoice for %s with %s '%s'. Order must be in PAID status",
                resourceName, fieldName, fieldValue));
    }
}
