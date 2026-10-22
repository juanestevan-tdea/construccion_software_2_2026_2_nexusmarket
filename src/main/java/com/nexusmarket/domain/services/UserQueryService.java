package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Consultas de usuarios.
 *
 * <p>Responsabilidad única: recuperar usuarios por identificador, email o rol y
 * traducir la ausencia a {@link ResourceNotFoundException} cuando el contrato lo
 * exige. No modifica estado.</p>
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepositoryPort userRepositoryPort;

    public User getUserByIdOrThrow(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User getUserByEmailOrThrow(String email) {
        return userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email '" + email + "' was not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepositoryPort.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    public List<User> findByRole(UserRole role) {
        return userRepositoryPort.findByRole(role);
    }
}
