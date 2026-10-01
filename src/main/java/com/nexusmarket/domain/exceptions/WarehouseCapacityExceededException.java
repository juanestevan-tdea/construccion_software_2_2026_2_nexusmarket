package com.nexusmarket.domain.exceptions;

public class WarehouseCapacityExceededException extends RuntimeException {

    public WarehouseCapacityExceededException(String message) {
        super(message);
    }
}
