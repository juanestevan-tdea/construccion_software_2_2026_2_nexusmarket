package com.nexusmarket.common.exception;

public class InvoiceNotPayableException extends RuntimeException {

    public InvoiceNotPayableException(String message) {
        super(message);
    }

    public InvoiceNotPayableException(String resource, String field, Object value) {
        super(String.format("Cannot issue invoice for %s with %s: '%s'", resource, field, value));
    }
}
