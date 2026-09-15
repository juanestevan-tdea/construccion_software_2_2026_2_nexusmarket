package com.nexusmarket.exception;

/**
 * Thrown when a requested resource does not exist in the system.
 * Maps to HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s with id '%s' was not found", resourceName, id));
    }
}
