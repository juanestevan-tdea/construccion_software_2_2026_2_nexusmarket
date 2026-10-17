package com.nexusmarket.auth.service;

import com.nexusmarket.adapters.useCases.AuthenticationUseCaseImpl;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.services.UserManagementService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenInvalidationServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-hs256-signing-at-least-256-bits-long!!";
    private static final long TTL_MS = 86400000L;

    private JwtService jwtService;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private SessionAuditService sessionAuditService;

    @Mock
    private UserUseCasePort userUseCasePort;

    @Mock
    private BuyerUseCasePort buyerUseCasePort;

    @Mock
    private SellerUseCasePort sellerUseCasePort;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticationUseCasePort authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TTL_MS);
        // Mockear getUserByIdOrThrow para logout/changePassword
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, "buyer@test.com", 1));
        authService = new AuthenticationUseCaseImpl(
                userUseCasePort, buyerUseCasePort, sellerUseCasePort, userManagementService,
                sessionAuditService, jwtService, authenticationManager, TTL_MS);
    }

    private static User user(Long id, String email, Integer tokenVersion) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName("Test User")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .tokenVersion(tokenVersion)
                .build();
    }

    private static UserDetailsAdapter adapterOf(User u) {
        return new UserDetailsAdapter(u);
    }

    // ---------- tokenInvalidatedAfterLogout ----------
    @Test
    @DisplayName("tokenInvalidatedAfterLogout: token válido se vuelve inválido tras logout")
    void tokenInvalidatedAfterLogout() {
        User u = user(1L, "buyer@test.com", 0);
        UserDetailsAdapter adapter = adapterOf(u);

        String token = jwtService.generateToken(adapter);
        assertThat(jwtService.validateToken(token, adapter)).isTrue();

        // logout incrementa tokenVersion a 1
        User updated = user(1L, "buyer@test.com", 1);
        when(userUseCasePort.invalidateSessions(1L)).thenReturn(updated);

        authService.logout(1L);

        UserDetailsAdapter updatedAdapter = adapterOf(updated);
        assertThat(jwtService.validateToken(token, updatedAdapter)).isFalse();
    }

    // ---------- tokenInvalidatedAfterPasswordChange ----------
    @Test
    @DisplayName("tokenInvalidatedAfterPasswordChange: token válido se vuelve inválido tras cambio de contraseña")
    void tokenInvalidatedAfterPasswordChange() {
        User u = user(1L, "buyer@test.com", 0);
        UserDetailsAdapter adapter = adapterOf(u);

        String token = jwtService.generateToken(adapter);
        assertThat(jwtService.validateToken(token, adapter)).isTrue();

        // cambio de contraseña incrementa tokenVersion a 1
        User updated = user(1L, "buyer@test.com", 1);
        when(userManagementService.changePassword(1L, "old-password", "NewPass123"))
                .thenReturn(updated);

        authService.changePassword(1L, "old-password", "NewPass123");

        UserDetailsAdapter updatedAdapter = adapterOf(updated);
        assertThat(jwtService.validateToken(token, updatedAdapter)).isFalse();
    }

    // ---------- tokenWithOldVersionRejected ----------
    @Test
    @DisplayName("tokenWithOldVersionRejected: token con ver obsoleto es rechazado")
    void tokenWithOldVersionRejected() {
        User u = user(1L, "buyer@test.com", 0);
        UserDetailsAdapter adapter = adapterOf(u);

        String token = jwtService.generateToken(adapter);
        assertThat(jwtService.validateToken(token, adapter)).isTrue();

        // misma sesión, pero versión incrementada en BD (sin regenerar token)
        User updated = user(1L, "buyer@test.com", 1);
        UserDetailsAdapter updatedAdapter = adapterOf(updated);

        assertThat(jwtService.validateToken(token, updatedAdapter)).isFalse();
    }

    // ---------- logoutIsIdempotent ----------
    @Test
    @DisplayName("logoutIsIdempotent: dos logouts seguidos no rompen")
    void logoutIsIdempotent() {
        User updated1 = user(1L, "buyer@test.com", 1);
        User updated2 = user(1L, "buyer@test.com", 2);
        when(userUseCasePort.invalidateSessions(1L))
                .thenReturn(updated1).thenReturn(updated2);

        authService.logout(1L); // primera vez
        authService.logout(1L); // segunda vez: idempotente, responde sin error

        verify(userUseCasePort, times(2)).invalidateSessions(1L);
    }

    // ---------- loginAfterLogoutWorks ----------
    @Test
    @DisplayName("loginAfterLogoutWorks: tras logout, nuevo login genera token válido con ver incrementado")
    void loginAfterLogoutWorks() {
        User u = user(1L, "buyer@test.com", 0);
        UserDetailsAdapter adapter = adapterOf(u);

        String tokenV0 = jwtService.generateToken(adapter);
        assertThat(jwtService.validateToken(tokenV0, adapter)).isTrue();

        // logout
        User updated = user(1L, "buyer@test.com", 1);
        when(userUseCasePort.invalidateSessions(1L)).thenReturn(updated);
        authService.logout(1L);

        // nuevo login genera token con ver=1
        UserDetailsAdapter updatedAdapter = adapterOf(updated);
        String tokenV1 = jwtService.generateToken(updatedAdapter);
        assertThat(jwtService.validateToken(tokenV1, updatedAdapter)).isTrue();

        // el token previo (ver=0) ya no es válido
        assertThat(jwtService.validateToken(tokenV0, updatedAdapter)).isFalse();
    }
}
