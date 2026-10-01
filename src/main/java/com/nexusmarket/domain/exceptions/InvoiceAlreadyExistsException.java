package com.nexusmarket.domain.exceptions;

public class InvoiceAlreadyExistsException extends RuntimeException {

    public InvoiceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s for %s '%s' already exists and is active", resourceName, fieldName, fieldValue));
    }
}
