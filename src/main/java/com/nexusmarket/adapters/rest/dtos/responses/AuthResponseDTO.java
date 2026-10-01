package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String tokenType;
    private String token;
    private Long expiresIn;
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;

    public static AuthResponseDTO fromEntity(User user, String token, long expiresIn) {
        return AuthResponseDTO.builder()
                .tokenType("Bearer")
                .token(token)
                .expiresIn(expiresIn)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
