package com.nexusmarket.domain.ports.in.model;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de una operación de autenticación.
 *
 * <p>Modelo propio de la capa de aplicación; el adaptador REST lo traduce a su
 * {@code AuthResponseDTO}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResult {

    private String tokenType;
    private String token;
    private Long expiresIn;
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;

    public static AuthenticationResult from(User user, String token, long expiresIn) {
        return AuthenticationResult.builder()
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
