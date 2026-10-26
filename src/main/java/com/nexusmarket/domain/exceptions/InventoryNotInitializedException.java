package com.nexusmarket.domain.exceptions;

/**
 * Se intentó operar sobre un inventario cuya cantidad no está inicializada
 * (HTTP 422).
 *
 * <p>Es una precondición de estado del agregado Inventario y no conoce Spring
 * ni HTTP.</p>
 */
public class InventoryNotInitializedException extends DomainException {

    public InventoryNotInitializedException(String message) {
        super(message);
    }
}
