package com.nexusmarket.domain.exceptions;

/**
 * Stock insuficiente para satisfacer una operación de inventario o de pedido.
 *
 * <p>Es una regla de negocio (HTTP 422). No conoce Spring ni HTTP: el adaptador
 * REST decide el código de estado.</p>
 */
public class InsufficientStockException extends DomainException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Object productId, int requested, int available) {
        super(String.format("Insufficient stock for product '%s': requested %d, available %d",
                productId, requested, available));
    }
}
