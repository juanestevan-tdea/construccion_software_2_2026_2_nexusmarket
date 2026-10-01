package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserUseCasePort {

    User createUser(String email, String fullName, String password, UserRole role);

    User getUserByIdOrThrow(Long id);

    User getUserByEmailOrThrow(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAll();

    List<User> findByRole(UserRole role);

    User blockUser(Long id);

    User activateUser(Long id);

    User changeRole(Long id, UserRole newRole);
}
