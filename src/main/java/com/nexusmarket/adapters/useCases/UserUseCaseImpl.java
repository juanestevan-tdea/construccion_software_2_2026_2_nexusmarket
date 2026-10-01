package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.UserManagementService;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCasePort {

    private final UserManagementService userManagementService;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User createUser(String email, String fullName, String password, UserRole role) {
        return userManagementService.createUser(email, fullName, password, role);
    }

    @Override
    public User getUserByIdOrThrow(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User getUserByEmailOrThrow(String email) {
        return userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email '" + email + "' was not found"));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepositoryPort.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userRepositoryPort.findByRole(role);
    }

    @Override
    public User blockUser(Long id) {
        return userManagementService.blockUser(id);
    }

    @Override
    public User activateUser(Long id) {
        return userManagementService.activateUser(id);
    }

    @Override
    public User changeRole(Long id, UserRole newRole) {
        return userManagementService.changeRole(id, newRole);
    }
}

