package com.nexusmarket.infrastructure.security;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String OTHER_SECRET = "fedcba9876543210fedcba9876543210";
    private static final long TTL_MS = 60_000L;

    private JwtService jwtService;
    private UserDetailsAdapter activeUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, TTL_MS);
        activeUser = new UserDetailsAdapter(user("buyer@test.com", UserStatus.ACTIVE));
    }

    @Test
    @DisplayName("token válido con versión actual es aceptado")
    void validTokenIsAccepted() {
        String token = jwtService.generateToken(activeUser);

        assertThat(jwtService.validateToken(token, activeUser)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("buyer@test.com");
        assertThat(jwtService.extractVersion(token)).isZero();
        assertThat(jwtService.extractJti(token)).isNotBlank();
    }

    @Test
    @DisplayName("token expirado es rechazado")
    void expiredTokenIsRejected() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        String expiredToken = Jwts.builder()
                .subject(activeUser.getUsername())
                .claim("ver", 0)
                .issuedAt(new Date(now - 120_000))
                .expiration(new Date(now - 60_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtService.validateToken(expiredToken, activeUser)).isFalse();
    }

    @Test
    @DisplayName("token manipulado es rechazado")
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(activeUser);
        String[] parts = token.split("\\.");
        char first = parts[1].charAt(0);
        parts[1] = (first == 'A' ? 'B' : 'A') + parts[1].substring(1);

        String tampered = String.join(".", parts);

        assertThat(jwtService.validateToken(tampered, activeUser)).isFalse();
    }

    @Test
    @DisplayName("token firmado con otra clave es rechazado")
    void tokenSignedWithDifferentKeyIsRejected() {
        JwtService otherIssuer = new JwtService(OTHER_SECRET, TTL_MS);
        String token = otherIssuer.generateToken(activeUser);

        assertThat(jwtService.validateToken(token, activeUser)).isFalse();
    }

    @Test
    @DisplayName("el token se firma explícitamente con HS256")
    void tokenUsesHs256() {
        String token = jwtService.generateToken(activeUser);
        String header = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                StandardCharsets.UTF_8);

        assertThat(header).contains("\"alg\":\"HS256\"");
    }

    @Test
    @DisplayName("la aplicación falla si JWT_SECRET falta, está vacío o es demasiado corto")
    void missingOrWeakSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService(null, TTL_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService("", TTL_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService("too-short", TTL_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("la expiración configurada debe ser positiva")
    void nonPositiveExpirationFailsFast() {
        assertThatThrownBy(() -> new JwtService(SECRET, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.expiration");
    }

    private static User user(String email, UserStatus status) {
        return User.builder()
                .id(1L)
                .email(email)
                .fullName("Test User")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(status)
                .tokenVersion(0)
                .build();
    }
}
