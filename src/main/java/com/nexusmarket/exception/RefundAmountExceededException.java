package com.nexusmarket.exception;

public class RefundAmountExceededException extends RuntimeException {

    public RefundAmountExceededException(String message) {
        super(message);
    }

    public RefundAmountExceededException(String resource, String field, Object value) {
        super(String.format("Refund amount exceeded for %s (%s: '%s')", resource, field, value));
    }
}
