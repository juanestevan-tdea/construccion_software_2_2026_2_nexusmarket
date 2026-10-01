package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.UserJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.UserJpaRepository;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserJpaMapper userJpaMapper;

    @Override
    public User save(User user) {
        if (user == null) {
            return null;
        }
        UserJpaEntity entity = userJpaMapper.toEntity(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return userJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(userJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userJpaRepository.findByRole(role).stream()
                .map(userJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return userJpaRepository.findByStatus(status).stream()
                .map(userJpaMapper::toDomain)
                .toList();
    }
}
