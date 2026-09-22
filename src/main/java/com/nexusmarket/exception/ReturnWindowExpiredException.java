package com.nexusmarket.exception;

public class ReturnWindowExpiredException extends RuntimeException {

    public ReturnWindowExpiredException(String message) {
        super(message);
    }

    public ReturnWindowExpiredException(String resource, String field, Object value) {
        super(String.format("Return window has expired for %s (%s: '%s')", resource, field, value));
    }
}
