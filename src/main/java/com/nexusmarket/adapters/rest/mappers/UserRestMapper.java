package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import com.nexusmarket.domain.models.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserRestMapper {

    public UserResponseDTO toResponseDTO(User domain) {
        if (domain == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .fullName(domain.getFullName())
                .role(domain.getRole())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
