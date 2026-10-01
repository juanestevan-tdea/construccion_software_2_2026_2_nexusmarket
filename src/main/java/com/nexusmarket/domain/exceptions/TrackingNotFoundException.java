package com.nexusmarket.domain.exceptions;

public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(String message) {
        super(message);
    }

    public TrackingNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' was not found", resourceName, fieldName, fieldValue));
    }
}
