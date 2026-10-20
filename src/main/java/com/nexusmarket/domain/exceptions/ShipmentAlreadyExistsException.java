package com.nexusmarket.domain.exceptions;

public class ShipmentAlreadyExistsException extends DomainException {

    public ShipmentAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s for %s '%s' already exists", resourceName, fieldName, fieldValue));
    }
}
