package com.nexusmarket.domain.exceptions;

/**
 * Ya existe un usuario registrado con el email indicado (HTTP 409).
 *
 * <p>Es una excepción específica del agregado Usuario; no conoce Spring ni
 * HTTP.</p>
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String email) {
        super(String.format("User with email '%s' already exists", email));
    }
}
