package com.nexusmarket.common.exception;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(String message) {
        super(message);
    }

    public ProductNotAvailableException(String resource, String field, Object value) {
        super(String.format("%s is not available with %s: '%s'", resource, field, value));
    }
}
