package com.nexusmarket.common.exception;

/**
 * Thrown when trying to create a resource that already exists, for example
 * registering a user with an email already in use. Maps to HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(String.format("%s already exists with %s '%s'", resourceName, field, value));
    }
}
