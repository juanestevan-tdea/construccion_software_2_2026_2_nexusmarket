package com.nexusmarket.domain.exceptions;

public class ReturnNotAllowedException extends RuntimeException {

    public ReturnNotAllowedException(String message) {
        super(message);
    }

    public ReturnNotAllowedException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Return not allowed for %s with %s '%s'", resourceName, fieldName, fieldValue));
    }
}
