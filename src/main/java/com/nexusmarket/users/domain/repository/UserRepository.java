package com.nexusmarket.users.domain.repository;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByStatus(UserStatus status);
    boolean existsByEmail(String email);
}