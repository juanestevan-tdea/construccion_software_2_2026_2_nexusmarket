package com.nexusmarket.domain.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s with id '%s' was not found", resourceName, id));
    }
}
