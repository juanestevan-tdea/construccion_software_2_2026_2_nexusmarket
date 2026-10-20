package com.nexusmarket.buyers.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.nexusmarket.adapters.rest.controllers.admin.BuyerManagementController;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.RestAccessDeniedHandler;
import com.nexusmarket.infrastructure.security.RestAuthenticationEntryPoint;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el estado comercial de un comprador solo lo pueda cambiar un
 * administrador.
 */
@WebMvcTest(BuyerManagementController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class BuyerManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuyerUseCasePort buyerUseCasePort;

    @MockitoBean
    private UserUseCasePort userUseCasePort;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionAuditService sessionAuditService;

    private static User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("user@test.com")
                .fullName("User")
                .password("$2a$10$hashed")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }

    private static Buyer buyer(BuyerCommercialStatus status) {
        return Buyer.builder()
                .id(20L)
                .userId(1L)
                .primaryAddress("Calle 1")
                .commercialStatus(status)
                .build();
    }

    @Test
    @DisplayName("un administrador puede bloquear a un comprador")
    void adminCanChangeCommercialStatus() throws Exception {
        when(buyerUseCasePort.changeCommercialStatus(20L, BuyerCommercialStatus.BLOCKED))
                .thenReturn(buyer(BuyerCommercialStatus.BLOCKED));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.BUYER));

        mockMvc.perform(patch("/api/v1/admin/buyers/20/status")
                        .param("newStatus", "BLOCKED")
                        .with(authenticatedAs(user(2L, UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.commercialStatus").value("BLOCKED"));

        verify(buyerUseCasePort).changeCommercialStatus(20L, BuyerCommercialStatus.BLOCKED);
    }

    @Test
    @DisplayName("un comprador no puede cambiar su propio estado comercial")
    void buyerCannotChangeCommercialStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/buyers/20/status")
                        .param("newStatus", "ACTIVE")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(buyerUseCasePort, never()).changeCommercialStatus(anyLong(), any());
    }

    @Test
    @DisplayName("un estado comercial inválido devuelve 400")
    void invalidCommercialStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/buyers/20/status")
                        .param("newStatus", "UNKNOWN")
                        .with(authenticatedAs(user(2L, UserRole.ADMIN))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
