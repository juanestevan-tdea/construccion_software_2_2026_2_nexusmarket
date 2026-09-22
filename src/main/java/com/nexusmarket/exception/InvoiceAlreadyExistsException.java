package com.nexusmarket.exception;

public class InvoiceAlreadyExistsException extends RuntimeException {

    public InvoiceAlreadyExistsException(String message) {
        super(message);
    }

    public InvoiceAlreadyExistsException(String resource, String field, Object value) {
        super(String.format("%s already exists for %s: '%s'", resource, field, value));
    }
}
