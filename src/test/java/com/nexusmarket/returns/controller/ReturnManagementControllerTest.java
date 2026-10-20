package com.nexusmarket.returns.controller;

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

import com.nexusmarket.adapters.rest.controllers.supervisor.ReturnManagementController;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.buyer.ReturnUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.ReturnStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.RestAccessDeniedHandler;
import com.nexusmarket.infrastructure.security.RestAuthenticationEntryPoint;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que las transiciones de una devolución solo las ejecute un
 * SUPERVISOR y no el comprador que la creó.
 */
@WebMvcTest(ReturnManagementController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class ReturnManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReturnUseCasePort returnUseCasePort;

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

    private static Return returnRequest(Long id, ReturnStatus status) {
        return Return.builder()
                .id(id)
                .orderId(100L)
                .reason("Producto defectuoso")
                .status(status)
                .build();
    }

    @Test
    @DisplayName("un supervisor puede aprobar una devolución")
    void supervisorCanApproveReturn() throws Exception {
        when(returnUseCasePort.approveReturn(50L))
                .thenReturn(returnRequest(50L, ReturnStatus.APPROVED));

        mockMvc.perform(patch("/api/v1/supervisor/returns/50/approve")
                        .with(authenticatedAs(user(3L, UserRole.SUPERVISOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(returnUseCasePort).approveReturn(50L);
    }

    @Test
    @DisplayName("un supervisor puede rechazar y completar una devolución")
    void supervisorCanRejectAndCompleteReturn() throws Exception {
        when(returnUseCasePort.rejectReturn(50L))
                .thenReturn(returnRequest(50L, ReturnStatus.REJECTED));
        when(returnUseCasePort.completeReturn(50L))
                .thenReturn(returnRequest(50L, ReturnStatus.COMPLETED));

        mockMvc.perform(patch("/api/v1/supervisor/returns/50/reject")
                        .with(authenticatedAs(user(3L, UserRole.SUPERVISOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(patch("/api/v1/supervisor/returns/50/complete")
                        .with(authenticatedAs(user(3L, UserRole.SUPERVISOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(returnUseCasePort).rejectReturn(50L);
        verify(returnUseCasePort).completeReturn(50L);
    }

    @Test
    @DisplayName("un comprador no puede aprobar su propia devolución")
    void buyerCannotApproveReturn() throws Exception {
        mockMvc.perform(patch("/api/v1/supervisor/returns/50/approve")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(returnUseCasePort, never()).approveReturn(50L);
    }

    @Test
    @DisplayName("aprobar una devolución inexistente devuelve 404")
    void approveMissingReturnReturns404() throws Exception {
        when(returnUseCasePort.approveReturn(999L))
                .thenThrow(new ResourceNotFoundException("Return", 999L));

        mockMvc.perform(patch("/api/v1/supervisor/returns/999/approve")
                        .with(authenticatedAs(user(3L, UserRole.SUPERVISOR))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
