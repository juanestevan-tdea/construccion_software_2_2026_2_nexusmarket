package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.UserAlreadyExistsException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.policies.PasswordPolicy;
import com.nexusmarket.domain.ports.out.PasswordHasherPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;

/**
 * Alta de usuarios.
 *
 * <p>Responsabilidad única: validar el email y persistir un usuario nuevo con
 * la contraseña cifrada y el estado inicial. Sin dependencias de Spring: el
 * bean se registra en {@code infrastructure/config/UserServiceConfig} y que la
 * operación sea transaccional es decisión de la capa de aplicación.</p>
 */
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public User createUser(String email, String fullName, String password, UserRole role) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
        PasswordPolicy.validate(password);

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordHasherPort.hash(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepositoryPort.save(user);
    }
}
