package com.nexusmarket.domain.policies;

import com.nexusmarket.domain.exceptions.BusinessRuleException;

import java.util.regex.Pattern;

/**
 * Política única de contraseñas para el dominio.
 *
 * <p>Centraliza la regla que comparten el registro, la creación administrativa
 * y el cambio de contraseña. Las anotaciones de los DTO validan la misma regla
 * en el borde HTTP; esta clase es la defensa que no depende del controlador.</p>
 */
public final class PasswordPolicy {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

    public static final String MESSAGE =
            "La contraseña debe tener al menos 8 caracteres (máximo 72), una letra y un número";

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static void validate(String password) {
        if (!isValid(password)) {
            throw new BusinessRuleException(MESSAGE);
        }
    }
}
