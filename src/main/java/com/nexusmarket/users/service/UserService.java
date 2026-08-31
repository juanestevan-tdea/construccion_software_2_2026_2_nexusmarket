package com.nexusmarket.users.service;

import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.model.UserStatus;
import com.nexusmarket.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Crear un nuevo usuario
    @Transactional
    public User createUser(String email, String fullName, String password, UserRole role) {
        // Validar que el email no exista
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(password) // En producción, esto debe estar encriptado con BCrypt
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    // Buscar usuario por email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Buscar usuario por ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Listar todos los usuarios
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Listar usuarios por rol
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    // Bloquear usuario
    @Transactional
    public User blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.block();
        return userRepository.save(user);
    }

    // Activar usuario
    @Transactional
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.activate();
        return userRepository.save(user);
    }

    // Cambiar rol de usuario (regla de negocio: cada usuario tiene un único rol)
    @Transactional
    public User changeRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }
}