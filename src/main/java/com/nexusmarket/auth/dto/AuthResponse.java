package com.nexusmarket.auth.dto;

import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Successful authentication payload returned by {@code POST /api/auth/login}
 * and {@code POST /api/auth/register}.
 *
 * <p>
 * It carries both the token and the basic profile data, including the role, so
 * a frontend can pick the right UI immediately without decoding the JWT
 * payload.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Always {@code "Bearer"}; tells the client how to build the header.
     */
    private String tokenType;

    /**
     * The signed JWT to send as {@code Authorization: Bearer <token>}.
     */
    private String token;

    /**
     * Token lifetime in milliseconds, so clients know when to re-authenticate.
     */
    private Long expiresIn;

    private Long id;
    private String email;
    private String fullName;
    private UserRole role;

    public static AuthResponse fromEntity(User user, String token, long expiresIn) {
        return AuthResponse.builder()
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
