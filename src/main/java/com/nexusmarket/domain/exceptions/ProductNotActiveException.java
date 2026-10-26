package com.nexusmarket.domain.exceptions;

/**
 * Se intentó añadir a una orden un producto inactivo (HTTP 422).
 *
 * <p>Extiende {@link BusinessRuleException} para conservar la compatibilidad
 * con los consumidores que ya trataban esta regla como una violación de
 * negocio, pero expresa el caso concreto del agregado Producto.</p>
 */
public class ProductNotActiveException extends BusinessRuleException {

    public ProductNotActiveException(Long productId) {
        super(String.format(
                "Product with id '%s' is not active and cannot be added to an order",
                productId));
    }
}
