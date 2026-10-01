package com.nexusmarket.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.adapters.rest.controllers.AuthController;
import com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.RegisterRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO;
import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.valueobjects.UserRole;
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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationUseCasePort authenticationUseCasePort;

    // ---------- POST /api/auth/register ----------
    @Test
    @DisplayName("register_BuyerRole_Returns201WithAuthResponse")
    void register_buyerRole_returns201WithAuthResponse() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("secret123")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123 #45-67")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token")
                .expiresIn(86400000L)
                .id(1L)
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .role(UserRole.BUYER)
                .build();

        when(authenticationUseCasePort.register(any(RegisterRequestDTO.class))).thenReturn(response);

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

        verify(authenticationUseCasePort).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_SellerRole_Returns201WithAuthResponse")
    void register_sellerRole_returns201WithAuthResponse() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("seller1@test.com")
                .fullName("Tienda SA")
                .password("secret123")
                .role(UserRole.SELLER)
                .taxId("900123456-7")
                .companyName("Tienda Nexus")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token.seller")
                .expiresIn(86400000L)
                .id(2L)
                .email("seller1@test.com")
                .fullName("Tienda SA")
                .role(UserRole.SELLER)
                .build();

        when(authenticationUseCasePort.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("seller1@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(authenticationUseCasePort).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_AdminRole_PropagatesBusinessRuleException")
    void register_adminRole_propagatesBusinessRuleException() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("hacker@test.com")
                .fullName("Evil User")
                .password("secret123")
                .role(UserRole.ADMIN)
                .build();

        when(authenticationUseCasePort.register(any(RegisterRequestDTO.class)))
                .thenThrow(new BusinessRuleException(
                        "Self-registration is only allowed for BUYER and SELLER roles"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

        verify(authenticationUseCasePort).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_InvalidEmail_Returns400AndNeverCallsService")
    void register_invalidEmail_returns400AndNeverCallsService() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
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

        verify(authenticationUseCasePort, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_BlankPassword_Returns400AndNeverCallsService")
    void register_blankPassword_returns400AndNeverCallsService() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
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

        verify(authenticationUseCasePort, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_ShortPassword_Returns400AndNeverCallsService")
    void register_shortPassword_returns400AndNeverCallsService() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
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

        verify(authenticationUseCasePort, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register_NullRole_Returns400AndNeverCallsService")
    void register_nullRole_returns400AndNeverCallsService() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
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

        verify(authenticationUseCasePort, never()).register(any(RegisterRequestDTO.class));
    }

    // ---------- POST /api/auth/login ----------
    @Test
    @DisplayName("login_ValidCredentials_Returns200WithToken")
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("buyer1@test.com")
                .password("secret123")
                .build();

        AuthResponseDTO response = AuthResponseDTO.builder()
                .tokenType("Bearer")
                .token("fake.jwt.token")
                .expiresIn(86400000L)
                .id(1L)
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .role(UserRole.BUYER)
                .build();

        when(authenticationUseCasePort.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("buyer1@test.com"))
                .andExpect(jsonPath("$.role").value("BUYER"));

        verify(authenticationUseCasePort).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login_InvalidCredentials_PropagatesBadCredentialsException")
    void login_invalidCredentials_propagatesBadCredentialsException() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("buyer1@test.com")
                .password("wrong-password")
                .build();

        when(authenticationUseCasePort.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authenticationUseCasePort).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login_BlankEmail_Returns400AndNeverCallsService")
    void login_blankEmail_returns400AndNeverCallsService() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationUseCasePort, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login_InvalidEmailFormat_Returns400AndNeverCallsService")
    void login_invalidEmailFormat_returns400AndNeverCallsService() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("not-an-email")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationUseCasePort, never()).login(any(LoginRequestDTO.class));
    }
}


