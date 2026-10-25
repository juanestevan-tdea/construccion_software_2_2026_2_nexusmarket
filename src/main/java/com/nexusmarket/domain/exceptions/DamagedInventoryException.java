package com.nexusmarket.domain.exceptions;

/**
 * Se intentó reservar o confirmar el pago de inventario marcado como dañado
 * (HTTP 422).
 *
 * <p>Es una regla de negocio del agregado Inventario y no conoce Spring ni
 * HTTP.</p>
 */
public class DamagedInventoryException extends DomainException {

    public DamagedInventoryException(String message) {
        super(message);
    }
}
