package com.nexusmarket.domain.exceptions;

/**
 * Ya existe una bodega con el nombre indicado (HTTP 409).
 */
public class WarehouseNameAlreadyExistsException extends DuplicateResourceException {

    public WarehouseNameAlreadyExistsException(String name) {
        super("Warehouse", "name", name);
    }
}
