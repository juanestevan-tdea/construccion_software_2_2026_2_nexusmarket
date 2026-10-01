package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

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
}
