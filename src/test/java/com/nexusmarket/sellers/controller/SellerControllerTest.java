package com.nexusmarket.sellers.controller;

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

import com.nexusmarket.adapters.rest.controllers.seller.SellerController;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el aislamiento del perfil de vendedor y que la creación use el
 * usuario del token.
 */
@WebMvcTest(SellerController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerUseCasePort sellerUseCasePort;

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

    private static Seller seller(Long id, Long userId) {
        return Seller.builder()
                .id(id)
                .userId(userId)
                .taxId("TAX-" + id)
                .companyName("Company " + id)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getSellerById permite al vendedor autenticado")
    void getSellerByIdAllowsSelf() throws Exception {
        when(sellerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.SELLER));

        mockMvc.perform(get("/api/v1/seller/sellers/10")
                        .with(authenticatedAs(user(1L, UserRole.SELLER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("getSellerById devuelve 403 para el perfil de otro vendedor")
    void getSellerByIdForbidsOtherSeller() throws Exception {
        when(sellerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));

        mockMvc.perform(get("/api/v1/seller/sellers/99")
                        .with(authenticatedAs(user(1L, UserRole.SELLER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(sellerUseCasePort, never()).getSellerByIdOrThrow(any());
    }

    @Test
    @DisplayName("createSeller usa el userId del token e ignora el del cuerpo")
    void createSellerUsesAuthenticatedUser() throws Exception {
        when(sellerUseCasePort.createSeller(1L, "TAX-10", "Company 10"))
                .thenReturn(seller(10L, 1L));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.SELLER));

        mockMvc.perform(post("/api/v1/seller/sellers")
                        .with(authenticatedAs(user(1L, UserRole.SELLER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":999,\"taxId\":\"TAX-10\",\"companyName\":\"Company 10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(sellerUseCasePort).createSeller(1L, "TAX-10", "Company 10");
    }

    @Test
    @DisplayName("getSellerByTaxId devuelve 403 si el taxId es de otro vendedor")
    void getSellerByTaxIdForbidsOtherSeller() throws Exception {
        when(sellerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));

        mockMvc.perform(get("/api/v1/seller/sellers/taxId")
                        .param("taxId", "TAX-99")
                        .with(authenticatedAs(user(1L, UserRole.SELLER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getAllSellers devuelve solo el perfil propio")
    void getAllSellersReturnsOnlySelf() throws Exception {
        when(sellerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(userUseCasePort.getUserByIdOrThrow(1L)).thenReturn(user(1L, UserRole.SELLER));

        mockMvc.perform(get("/api/v1/seller/sellers")
                        .with(authenticatedAs(user(1L, UserRole.SELLER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));

        verify(sellerUseCasePort, never()).findAll();
    }
}
