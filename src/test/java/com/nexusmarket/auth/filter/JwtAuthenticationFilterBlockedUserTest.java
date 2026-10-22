package com.nexusmarket.auth.filter;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtAuthenticationFilter;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterBlockedUserTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final long TTL_MS = 60_000L;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private SessionAuditService sessionAuditService;

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtService = new JwtService(SECRET, TTL_MS);
        filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, sessionAuditService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("un usuario bloqueado no queda autenticado aunque su JWT sea válido")
    void blockedUserIsNotAuthenticated() throws Exception {
        UserDetailsAdapter blocked = new UserDetailsAdapter(user("blocked@test.com", UserStatus.BLOCKED, 1));
        String token = jwtService.generateToken(blocked);
        when(customUserDetailsService.loadUserByUsername("blocked@test.com")).thenReturn(blocked);

        MockHttpServletRequest request = requestWith(token);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(sessionAuditService).recordTokenRejected(
                eq("blocked@test.com"), anyString(), eq("user disabled"),
                anyString(), anyString(), eq(1));
    }

    @Test
    @DisplayName("un JWT emitido antes del bloqueo deja de autenticar tras el bloqueo")
    void tokenIssuedBeforeBlockIsRejectedAfterBlock() throws Exception {
        UserDetailsAdapter beforeBlock = new UserDetailsAdapter(
                user("blocked@test.com", UserStatus.ACTIVE, 0));
        String token = jwtService.generateToken(beforeBlock);

        UserDetailsAdapter afterBlock = new UserDetailsAdapter(
                user("blocked@test.com", UserStatus.BLOCKED, 1));
        when(customUserDetailsService.loadUserByUsername("blocked@test.com"))
                .thenReturn(afterBlock);

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(sessionAuditService).recordTokenRejected(
                eq("blocked@test.com"), anyString(), eq("user disabled"),
                anyString(), anyString(), eq(0));
    }

    @Test
    @DisplayName("un usuario activo con JWT válido queda autenticado")
    void activeUserIsAuthenticated() throws Exception {
        UserDetailsAdapter active = new UserDetailsAdapter(user("active@test.com", UserStatus.ACTIVE, 0));
        String token = jwtService.generateToken(active);
        when(customUserDetailsService.loadUserByUsername("active@test.com")).thenReturn(active);

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isSameAs(active);
        verifyNoInteractions(sessionAuditService);
    }

    private static MockHttpServletRequest requestWith(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private static User user(String email, UserStatus status, int tokenVersion) {
        return User.builder()
                .id(1L)
                .email(email)
                .fullName("Test User")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(status)
                .tokenVersion(tokenVersion)
                .build();
    }
}
