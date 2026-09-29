package com.nexusmarket.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.auth.dto.AuthResponse;
import com.nexusmarket.auth.dto.LoginRequest;
import com.nexusmarket.auth.dto.RegisterRequest;
import com.nexusmarket.auth.service.AuthService;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.users.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC slice tests for {@link AuthController}.
 *
 * <p>
 * {@code @WebMvcTest} instantiates only the web layer: the controller, the JSON
 * converters and MockMvc. It does <b>not</b> build the security filter chain
 * ({@code SecurityConfig} is not imported here), so these tests deliberately cover
 * only the controller contract: routing, bean validation and JSON serialization.
 * Real 401/403 behaviour is exercised in {@code UserControllerTest}, which imports
 * the security configuration on purpose.</p>
 *
 * <p>Security filters are switched off with {@code addFilters = false} on purpose.
 * With them enabled, Spring Boot's default CSRF protection (which is <em>not</em> part
 * of the production configuration) would answer 403 to every POST, and the tests
 * would be validating a filter that does not exist in the real application.</p>
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // ---------- POST /api/auth/register ----------
    @Test
    @DisplayName("register_BuyerRole_Returns201WithAuthResponse")
    void register_buyerRole_returns201WithAuthResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("secret123")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123 #45-67")
                .build();

        AuthResponse response = AuthResponse.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token")
                .expiresIn(86400000L)
                .id(1L)
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .role(UserRole.BUYER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.expiresIn").value(86400000))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("buyer1@test.com"))
                .andExpect(jsonPath("$.fullName").value("Juan Perez"))
                .andExpect(jsonPath("$.role").value("BUYER"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_SellerRole_Returns201WithAuthResponse")
    void register_sellerRole_returns201WithAuthResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("seller1@test.com")
                .fullName("Tienda SA")
                .password("secret123")
                .role(UserRole.SELLER)
                .taxId("900123456-7")
                .companyName("Tienda Nexus")
                .build();

        AuthResponse response = AuthResponse.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token.seller")
                .expiresIn(86400000L)
                .id(2L)
                .email("seller1@test.com")
                .fullName("Tienda SA")
                .role(UserRole.SELLER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("seller1@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_AdminRole_PropagatesBusinessRuleException")
    void register_adminRole_propagatesBusinessRuleException() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("hacker@test.com")
                .fullName("Evil User")
                .password("secret123")
                .role(UserRole.ADMIN)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessRuleException(
                        "Self-registration is only allowed for BUYER and SELLER roles"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_InvalidEmail_Returns400AndNeverCallsService")
    void register_invalidEmail_returns400AndNeverCallsService() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .fullName("Juan Perez")
                .password("secret123")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_BlankPassword_Returns400AndNeverCallsService")
    void register_blankPassword_returns400AndNeverCallsService() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_ShortPassword_Returns400AndNeverCallsService")
    void register_shortPassword_returns400AndNeverCallsService() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("123")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register_NullRole_Returns400AndNeverCallsService")
    void register_nullRole_returns400AndNeverCallsService() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("secret123")
                .role(null)
                .primaryAddress("Calle 123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    // ---------- POST /api/auth/login ----------
    @Test
    @DisplayName("login_ValidCredentials_Returns200WithToken")
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("buyer1@test.com")
                .password("secret123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token")
                .expiresIn(86400000L)
                .id(1L)
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .role(UserRole.BUYER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("buyer1@test.com"))
                .andExpect(jsonPath("$.role").value("BUYER"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("login_InvalidCredentials_PropagatesBadCredentialsException")
    void login_invalidCredentials_propagatesBadCredentialsException() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("buyer1@test.com")
                .password("wrong-password")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("login_BlankEmail_Returns400AndNeverCallsService")
    void login_blankEmail_returns400AndNeverCallsService() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("login_InvalidEmailFormat_Returns400AndNeverCallsService")
    void login_invalidEmailFormat_returns400AndNeverCallsService() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("not-an-email")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }
}
