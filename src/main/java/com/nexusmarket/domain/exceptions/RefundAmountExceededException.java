package com.nexusmarket.domain.exceptions;

public class RefundAmountExceededException extends RuntimeException {

    public RefundAmountExceededException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Refund amount exceeded for %s with %s '%s'", resourceName, fieldName, fieldValue));
    }
}
