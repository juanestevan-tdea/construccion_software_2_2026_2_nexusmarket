package com.nexusmarket.buyers.controller;

import java.util.List;
import java.util.Optional;

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

import com.nexusmarket.adapters.rest.controllers.buyer.BuyerController;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el comprador solo pueda operar sobre su propio perfil y que la
 * identidad se resuelva desde el token, no desde el cuerpo.
 */
@WebMvcTest(BuyerController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class BuyerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuyerUseCasePort buyerUseCasePort;

    @MockitoBean
    private BuyerRepositoryPort buyerRepositoryPort;

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

    private static Buyer buyer(Long id, Long userId) {
        return Buyer.builder()
                .id(id)
                .userId(userId)
                .primaryAddress("Calle 1")
                .commercialStatus(BuyerCommercialStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("getBuyerById permite al comprador autenticado")
    void getBuyerByIdAllowsSelf() throws Exception {
        when(buyerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L, 1L)));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.BUYER));

        mockMvc.perform(get("/api/v1/buyer/buyers/20")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    @DisplayName("getBuyerById devuelve 403 para el perfil de otro comprador")
    void getBuyerByIdForbidsOtherBuyer() throws Exception {
        when(buyerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L, 1L)));

        mockMvc.perform(get("/api/v1/buyer/buyers/99")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(buyerUseCasePort, never()).getBuyerByIdOrThrow(any());
    }

    @Test
    @DisplayName("createBuyer usa el userId del token e ignora el del cuerpo")
    void createBuyerUsesAuthenticatedUser() throws Exception {
        when(buyerUseCasePort.createBuyer(1L, "Calle 1")).thenReturn(buyer(20L, 1L));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.BUYER));

        mockMvc.perform(post("/api/v1/buyer/buyers")
                        .with(authenticatedAs(user(1L, UserRole.BUYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":999,\"primaryAddress\":\"Calle 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20));

        verify(buyerUseCasePort).createBuyer(1L, "Calle 1");
    }

    @Test
    @DisplayName("getAllBuyers devuelve solo el perfil propio")
    void getAllBuyersReturnsOnlySelf() throws Exception {
        when(buyerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L, 1L)));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.BUYER));

        mockMvc.perform(get("/api/v1/buyer/buyers")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(20));

        verify(buyerUseCasePort, never()).findAll();
    }

    @Test
    @DisplayName("un vendedor no puede acceder a las rutas de comprador")
    void sellerCannotAccessBuyerRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/buyers")
                        .with(authenticatedAs(user(2L, UserRole.SELLER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("el cambio de estado comercial dejó de estar expuesto al comprador")
    void buyerCannotChangeOwnCommercialStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/buyer/buyers/20/status")
                        .param("newStatus", "ACTIVE")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isNotFound());

        verify(buyerUseCasePort, never()).changeCommercialStatus(any(), any());
    }
}
