package com.nexusmarket.common.exception;

public class ShipmentAlreadyExistsException extends RuntimeException {

    public ShipmentAlreadyExistsException(String message) {
        super(message);
    }

    public ShipmentAlreadyExistsException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value));
    }
}
