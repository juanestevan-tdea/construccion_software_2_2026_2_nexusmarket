package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    List<User> findAll();

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);
}
