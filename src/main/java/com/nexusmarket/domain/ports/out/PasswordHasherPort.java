package com.nexusmarket.domain.ports.out;

/**
 * Puerto de salida para el cifrado y verificación de contraseñas.
 *
 * <p>
 * Mantiene el dominio libre de dependencias de Spring Security: la
 * implementación concreta (BCrypt) vive en el adaptador de infraestructura.</p>
 */
public interface PasswordHasherPort {

    /**
     * Cifra una contraseña en claro.
     *
     * @param rawPassword contraseña en claro
     * @return hash de la contraseña
     */
    String hash(String rawPassword);

    /**
     * Comprueba si una contraseña en claro corresponde a un hash.
     *
     * @param rawPassword contraseña en claro
     * @param hashedPassword hash almacenado
     * @return {@code true} si coinciden
     */
    boolean matches(String rawPassword, String hashedPassword);
}
