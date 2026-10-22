package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.policies.PasswordPolicy;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alta de usuarios.
 *
 * <p>Responsabilidad única: validar el email y persistir un usuario nuevo con
 * la contraseña cifrada y el estado inicial.</p>
 */
@Service
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String fullName, String password, UserRole role) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        PasswordPolicy.validate(password);

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepositoryPort.save(user);
    }
}
