package com.nexusmarket.domain.ports.in;

import com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.RegisterRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO;

public interface AuthenticationUseCasePort {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Invalida todas las sesiones activas del usuario.
     *
     * @param userId identificador del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    void logout(Long userId);

    /**
     * Cambia la contraseña del usuario tras verificar la actual con BCrypt.
     * Invalida todas las sesiones activas tras el cambio.
     *
     * @param userId         identificador del usuario
     * @param currentPassword contraseña actual en texto plano
     * @param newPassword    nueva contraseña en texto plano
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws BusinessRuleException   si la contraseña actual es incorrecta o la nueva no cumple la política
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
