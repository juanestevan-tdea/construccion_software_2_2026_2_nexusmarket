package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.policies.PasswordPolicy;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cambio de contraseña de un usuario autenticado.
 *
 * <p>Responsabilidad única: verificar la contraseña actual, aplicar la política,
 * almacenar el nuevo hash e invalidar las sesiones activas.</p>
 */
@Service
@RequiredArgsConstructor
public class PasswordChangeService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }

        PasswordPolicy.validate(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.invalidateSessions();
        return userRepositoryPort.save(user);
    }
}
