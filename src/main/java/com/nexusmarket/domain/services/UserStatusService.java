package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cambios de estado y rol de usuarios.
 *
 * <p>Responsabilidad única: aplicar transiciones administrativas sobre el
 * usuario. El bloqueo revoca las sesiones activas mediante el propio modelo de
 * dominio.</p>
 */
@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public User blockUser(Long id) {
        User user = getByIdOrThrow(id);
        user.block();
        // Un bloqueo debe revocar los JWT ya emitidos, no solo impedir el login.
        user.invalidateSessions();
        return userRepositoryPort.save(user);
    }

    @Transactional
    public User activateUser(Long id) {
        User user = getByIdOrThrow(id);
        user.activate();
        return userRepositoryPort.save(user);
    }

    @Transactional
    public User changeRole(Long id, UserRole newRole) {
        User user = getByIdOrThrow(id);
        user.setRole(newRole);
        return userRepositoryPort.save(user);
    }

    private User getByIdOrThrow(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
