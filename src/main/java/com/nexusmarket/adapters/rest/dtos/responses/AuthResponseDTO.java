package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.model.AuthenticationResult;
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

    public static AuthResponseDTO fromResult(AuthenticationResult result) {
        return AuthResponseDTO.builder()
                .tokenType(result.getTokenType())
                .token(result.getToken())
                .expiresIn(result.getExpiresIn())
                .id(result.getId())
                .email(result.getEmail())
                .fullName(result.getFullName())
                .role(result.getRole())
                .build();
    }
}
