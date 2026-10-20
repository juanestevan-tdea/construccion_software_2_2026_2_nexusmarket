package com.nexusmarket.domain.exceptions;

public class WarehouseCapacityExceededException extends DomainException {

    public WarehouseCapacityExceededException(String message) {
        super(message);
    }
}
