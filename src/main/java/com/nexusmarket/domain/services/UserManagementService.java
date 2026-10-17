package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final Pattern PASSWORD_POLICY = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String fullName, String password, UserRole role) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepositoryPort.save(user);
    }

    @Transactional
    public User blockUser(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.block();
        return userRepositoryPort.save(user);
    }

    @Transactional
    public User activateUser(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.activate();
        return userRepositoryPort.save(user);
    }

    @Transactional
    public User changeRole(Long id, UserRole newRole) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setRole(newRole);
        return userRepositoryPort.save(user);
    }

    /**
     * Invalida todas las sesiones activas del usuario incrementando su tokenVersion.
     * Cualquier JWT emitido con un ver anterior quedará rechazado.
     *
     * @param userId identificador del usuario
     * @return el usuario actualizado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional
    public User invalidateSessions(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.invalidateSessions();
        return userRepositoryPort.save(user);
    }

    /**
     * Cambia la contraseña de un usuario tras verificar la contraseña actual
     * con BCrypt y validar la política de la nueva contraseña.
     * Invalida todas las sesiones activas tras el cambio.
     *
     * @param userId         identificador del usuario
     * @param currentPassword contraseña actual en texto plano
     * @param newPassword    nueva contraseña en texto plano
     * @return el usuario actualizado
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws BusinessRuleException   si la contraseña actual es incorrecta o la nueva no cumple la política
     */
    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }

        if (!isValidPassword(newPassword)) {
            throw new BusinessRuleException(
                    "La nueva contraseña debe tener al menos 8 caracteres, una letra y un número");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.invalidateSessions();
        return userRepositoryPort.save(user);
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_POLICY.matcher(password).matches();
    }
}

