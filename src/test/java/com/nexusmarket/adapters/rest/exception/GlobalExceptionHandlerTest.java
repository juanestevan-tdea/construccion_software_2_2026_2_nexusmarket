package com.nexusmarket.adapters.rest.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.DomainException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato HTTP del {@link GlobalExceptionHandler} sin levantar el
 * contexto de Spring: cada excepción de dominio debe traducirse a un código
 * semántico y a un cuerpo {@code ErrorResponse} uniforme.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException -> 404")
    void resourceNotFoundMapsTo404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product with id '7' was not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    @DisplayName("BusinessRuleException -> 422")
    void businessRuleMapsTo422() throws Exception {
        mockMvc.perform(get("/test/business-rule"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Order cannot be cancelled"));
    }

    @Test
    @DisplayName("DuplicateResourceException -> 409")
    void duplicateResourceMapsTo409() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with email 'a@b.com' already exists"));
    }

    @Test
    @DisplayName("AuthenticationException -> 401 sin revelar el motivo")
    void authenticationFailureMapsTo401() throws Exception {
        mockMvc.perform(get("/test/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("AccessDeniedException -> 403 sin revelar detalles internos")
    void accessDeniedMapsTo403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }

    @Test
    @DisplayName("InvalidStatusTransitionException -> 409 con detalles")
    void invalidTransitionMapsTo409WithDetails() throws Exception {
        mockMvc.perform(get("/test/transition"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.details[0]").value("Current status: CART"))
                .andExpect(jsonPath("$.details[1]").value("Target status: PAID"));
    }

    @Test
    @DisplayName("DomainException no mapeada -> 400 (no 500)")
    void unmappedDomainExceptionMapsTo400() throws Exception {
        mockMvc.perform(get("/test/domain"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("domain violation"));
    }

    @Test
    @DisplayName("Excepción no controlada -> 500 sin filtrar detalles internos")
    void unexpectedExceptionMapsTo500() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Product", 7L);
        }

        @GetMapping("/test/business-rule")
        void businessRule() {
            throw new BusinessRuleException("Order cannot be cancelled");
        }

        @GetMapping("/test/duplicate")
        void duplicate() {
            throw new DuplicateResourceException("User", "email", "a@b.com");
        }

        @GetMapping("/test/authentication")
        void authentication() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/test/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("internal ownership detail");
        }

        @GetMapping("/test/transition")
        void transition() {
            throw new InvalidStatusTransitionException("CART", "PAID");
        }

        @GetMapping("/test/domain")
        void domain() {
            throw new UnmappedDomainException("domain violation");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("database password leaked");
        }
    }

    static class UnmappedDomainException extends DomainException {
        UnmappedDomainException(String message) {
            super(message);
        }
    }
}
