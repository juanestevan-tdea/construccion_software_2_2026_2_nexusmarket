package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import com.nexusmarket.domain.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserJpaMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .password(entity.getPassword())
                .role(entity.getRole())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .fullName(domain.getFullName())
                .password(domain.getPassword())
                .role(domain.getRole())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
