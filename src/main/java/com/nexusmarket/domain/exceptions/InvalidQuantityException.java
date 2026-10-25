package com.nexusmarket.domain.exceptions;

/**
 * Cantidad inválida para una operación de dominio (cero, negativa o nula).
 *
 * <p>Es una regla de negocio (HTTP 422) y no conoce Spring ni HTTP.</p>
 */
public class InvalidQuantityException extends DomainException {

    public InvalidQuantityException(String message) {
        super(message);
    }
}
