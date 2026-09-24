package com.nexusmarket.common.exception;

public class RefundNotAllowedException extends RuntimeException {

    public RefundNotAllowedException(String message) {
        super(message);
    }

    public RefundNotAllowedException(String resource, String field, Object value) {
        super(String.format("Refund not allowed for %s with %s: '%s'", resource, field, value));
    }
}
