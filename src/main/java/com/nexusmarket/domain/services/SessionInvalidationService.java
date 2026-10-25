package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

/**
 * Invalidación explícita de sesiones de un usuario.
 *
 * <p>Responsabilidad única: incrementar {@code tokenVersion} para que los JWT
 * emitidos con la versión anterior dejen de ser aceptados.</p>
 */
@RequiredArgsConstructor
public class SessionInvalidationService {

    private final UserRepositoryPort userRepositoryPort;

    public User invalidateSessions(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.invalidateSessions();
        return userRepositoryPort.save(user);
    }
}
