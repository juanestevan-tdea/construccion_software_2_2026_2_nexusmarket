package com.nexusmarket.common.exception;

public class ReturnNotAllowedException extends RuntimeException {

    public ReturnNotAllowedException(String message) {
        super(message);
    }

    public ReturnNotAllowedException(String resource, String field, Object value) {
        super(String.format("Return not allowed for %s with %s: '%s'", resource, field, value));
    }
}
