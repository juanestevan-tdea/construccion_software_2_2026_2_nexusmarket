package com.nexusmarket.adapters.rest.controllers.supervisor;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.nexusmarket.domain.exceptions.InvalidRefundAmountException;
import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.supervisor.RefundUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP del controlador de reembolsos: autenticación de SUPERVISOR,
 * traducción de excepciones de dominio y cuerpo uniforme {@code ErrorResponse}.
 */
@WebMvcTest(RefundController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefundUseCasePort refundUseCasePort;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionAuditService sessionAuditService;

    private static User supervisor() {
        return User.builder()
                .id(3L)
                .email("supervisor@test.com")
                .fullName("Supervisor")
                .password("$2a$10$hashed")
                .role(UserRole.SUPERVISOR)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }

    @Test
    @DisplayName("createRefund devuelve 422 cuando el monto no es positivo")
    void createRefundReturns422WhenAmountIsInvalid() throws Exception {
        when(refundUseCasePort.createRefund(anyLong(), any(), any(BigDecimal.class)))
                .thenThrow(new InvalidRefundAmountException(
                        "Refund amount must be greater than zero"));

        mockMvc.perform(post("/api/v1/supervisor/refunds")
                        .with(authenticatedAs(supervisor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invoiceId\":1,\"returnRequestId\":99,\"amount\":10.00}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Refund amount must be greater than zero"))
                .andExpect(jsonPath("$.path").value("/api/v1/supervisor/refunds"));

        verify(refundUseCasePort).createRefund(anyLong(), any(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("createRefund devuelve 422 cuando la devolución no pertenece a la factura")
    void createRefundReturns422WhenRefundIsNotAllowed() throws Exception {
        when(refundUseCasePort.createRefund(anyLong(), any(), any(BigDecimal.class)))
                .thenThrow(new RefundNotAllowedException("Refund", "returnRequestId", 99L));

        mockMvc.perform(post("/api/v1/supervisor/refunds")
                        .with(authenticatedAs(supervisor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invoiceId\":1,\"returnRequestId\":99,\"amount\":10.00}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Refund not allowed for Refund with returnRequestId '99'"))
                .andExpect(jsonPath("$.path").value("/api/v1/supervisor/refunds"));

        verify(refundUseCasePort).createRefund(anyLong(), any(), any(BigDecimal.class));
    }
}
