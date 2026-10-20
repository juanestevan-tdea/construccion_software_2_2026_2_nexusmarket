package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.ports.in.model.AuthenticationResult;
import com.nexusmarket.domain.ports.in.model.LoginCommand;
import com.nexusmarket.domain.ports.in.model.RegisterCommand;

/**
 * Puerto de entrada para autenticación.
 *
 * <p>Los parámetros son modelos propios de la aplicación
 * ({@link RegisterCommand}, {@link LoginCommand}) y el resultado
 * {@link AuthenticationResult}. Este paquete no debe importar nada de
 * {@code com.nexusmarket.adapters}.</p>
 */
public interface AuthenticationUseCasePort {

    AuthenticationResult register(RegisterCommand command);

    AuthenticationResult login(LoginCommand command);

    /**
     * Invalida todas las sesiones activas del usuario.
     *
     * @param userId identificador del usuario
     * @throws com.nexusmarket.domain.exceptions.ResourceNotFoundException si el usuario no existe
     */
    void logout(Long userId);

    /**
     * Cambia la contraseña del usuario tras verificar la actual con BCrypt.
     * Invalida todas las sesiones activas tras el cambio.
     *
     * @param userId          identificador del usuario
     * @param currentPassword contraseña actual en texto plano
     * @param newPassword     nueva contraseña en texto plano
     * @throws com.nexusmarket.domain.exceptions.ResourceNotFoundException si el usuario no existe
     * @throws com.nexusmarket.domain.exceptions.BusinessRuleException   si la contraseña actual es incorrecta
     *                                                                   o la nueva no cumple la política
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
