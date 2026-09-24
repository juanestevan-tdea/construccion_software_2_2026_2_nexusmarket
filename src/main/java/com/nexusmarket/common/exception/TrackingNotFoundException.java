package com.nexusmarket.common.exception;

public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(String message) {
        super(message);
    }

    public TrackingNotFoundException(String resource, String field, Object value) {
        super(String.format("%s with %s '%s' was not found", resource, field, value));
    }
}
