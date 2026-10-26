package com.nexusmarket.domain.exceptions;

/**
 * Ya existe un usuario con el email indicado (HTTP 409).
 *
 * <p>Extiende {@link UserAlreadyExistsException} para conservar la
 * compatibilidad con los consumidores que ya capturaban el caso de duplicado
 * de usuario, pero deja explícito que la clave duplicada es el email.</p>
 */
public class UserEmailAlreadyExistsException extends UserAlreadyExistsException {

    public UserEmailAlreadyExistsException(String email) {
        super(email);
    }
}
