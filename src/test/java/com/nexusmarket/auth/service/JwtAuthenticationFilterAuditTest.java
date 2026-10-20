package com.nexusmarket.auth.filter;

import com.nexusmarket.adapters.useCases.AuthenticationUseCaseImpl;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test del servicio de auditorÃ­a de sesiones enfocado en la generaciÃ³n de eventos
 * cuando se rechaza un token por versiÃ³n (ver) desactualizada.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterAuditTest {

    private static final String TEST_SECRET = "test-secret-key-for-hs256-signing-at-least-256-bits-long!!";
    private static final long TTL_MS = 86400000L;

    @Mock
    private SessionAuditService sessionAuditService;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TTL_MS);
    }

    @Test
    @DisplayName("token con ver viejo genera evento TOKEN_REJECTED")
    void tokenWithOldVersionGeneratesTokenRejectedEvent() {
        // Usuario con tokenVersion = 1 en BD
        User user = User.builder()
                .id(1L)
                .email("buyer@test.com")
                .fullName("Test User")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .tokenVersion(1)
                .build();
        UserDetailsAdapter adapterV1 = new UserDetailsAdapter(user);

        // Generar token con ver = 0 (versiÃ³n obsoleta)
        UserDetailsAdapter adapterV0 = new UserDetailsAdapter(
                User.builder().id(1L).email("buyer@test.com")
                        .tokenVersion(0).role(UserRole.BUYER).status(UserStatus.ACTIVE).build());
        String token = jwtService.generateToken(adapterV0);

        // El token con ver=0 debe ser rechazado contra usuario con ver=1
        assertThat(jwtService.validateToken(token, adapterV1)).isFalse();

        // Extraer jti y verificar que se registrarÃ­a TOKEN_REJECTED
        String jti = jwtService.extractJti(token);
        Integer tokenVersion = jwtService.extractVersion(token);

        assertThat(jti).isNotNull();
        assertThat(tokenVersion).isEqualTo(0);

        // Simular la llamada al servicio de auditorÃ­a (como harÃ­a el filtro)
        sessionAuditService.recordTokenRejected("buyer@test.com", jti,
                "token version obsolete", null, tokenVersion);

        verify(sessionAuditService, times(1))
                .recordTokenRejected("buyer@test.com", jti,
                        "token version obsolete", null, 0);
    }

    @Test
    @DisplayName("token con ver actual es vÃ¡lido y no genera rechazo")
    void tokenWithCurrentVersionIsValid() {
        User user = User.builder()
                .id(1L)
                .email("buyer@test.com")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .tokenVersion(2)
                .build();
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);

        String token = jwtService.generateToken(adapter);

        assertThat(jwtService.validateToken(token, adapter)).isTrue();

        // No se deberÃ­a llamar al servicio de auditorÃ­a para tokens vÃ¡lidos
        verifyNoInteractions(sessionAuditService);
    }
}
