package com.nexusmarket.exception;

public class WarehouseCapacityExceededException extends RuntimeException {

    public WarehouseCapacityExceededException(String message) {
        super(message);
    }

    public WarehouseCapacityExceededException(String resource, String field, Object value) {
        super(String.format("Capacity exceeded for %s (%s: '%s')", resource, field, value));
    }
}
