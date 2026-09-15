package com.nexusmarket.exception;

/**
 * Thrown when a business rule is violated (e.g. negative inventory,
 * modifying a finished order).
 * Maps to HTTP 422 (Unprocessable Entity).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
