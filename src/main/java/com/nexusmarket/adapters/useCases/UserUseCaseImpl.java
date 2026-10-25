package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.services.SessionInvalidationService;
import com.nexusmarket.domain.services.UserCreationService;
import com.nexusmarket.domain.services.UserQueryService;
import com.nexusmarket.domain.services.UserStatusService;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de entrada para la administración de usuarios.
 *
 * <p>No accede a repositorios ni implementa reglas: delega cada operación en el
 * servicio de dominio especializado. Aquí, en la frontera del caso de uso, sí
 * se declaran las transacciones (P2-03).</p>
 */
@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCasePort {

    private final UserCreationService userCreationService;
    private final UserQueryService userQueryService;
    private final UserStatusService userStatusService;
    private final SessionInvalidationService sessionInvalidationService;

    @Override
    @Transactional
    public User createUser(String email, String fullName, String password, UserRole role) {
        return userCreationService.createUser(email, fullName, password, role);
    }

    @Override
    public User getUserByIdOrThrow(Long id) {
        return userQueryService.getUserByIdOrThrow(id);
    }

    @Override
    public User getUserByEmailOrThrow(String email) {
        return userQueryService.getUserByEmailOrThrow(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userQueryService.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userQueryService.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userQueryService.findAll();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userQueryService.findByRole(role);
    }

    @Override
    public User blockUser(Long id) {
        return userStatusService.blockUser(id);
    }

    @Override
    public User activateUser(Long id) {
        return userStatusService.activateUser(id);
    }

    @Override
    public User changeRole(Long id, UserRole newRole) {
        return userStatusService.changeRole(id, newRole);
    }

    @Override
    public User invalidateSessions(Long userId) {
        return sessionInvalidationService.invalidateSessions(userId);
    }
}
