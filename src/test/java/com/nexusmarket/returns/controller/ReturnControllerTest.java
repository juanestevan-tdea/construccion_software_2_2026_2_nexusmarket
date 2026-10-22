package com.nexusmarket.returns.controller;

import com.nexusmarket.adapters.rest.controllers.buyer.ReturnController;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.ReturnUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.ReturnStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.RestAccessDeniedHandler;
import com.nexusmarket.infrastructure.security.RestAuthenticationEntryPoint;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReturnController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class ReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReturnUseCasePort returnUseCasePort;

    @MockitoBean
    private BuyerUseCasePort buyerUseCasePort;

    @MockitoBean
    private OrderUseCasePort orderUseCasePort;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionAuditService sessionAuditService;

    @Test
    @DisplayName("lista solo las devoluciones del comprador autenticado")
    void listsReturnsForAuthenticatedBuyer() throws Exception {
        Buyer buyer = buyer(20L, 7L);
        Return returned = returnRequest(50L, 100L);
        when(buyerUseCasePort.findByUserId(7L)).thenReturn(Optional.of(buyer));
        when(returnUseCasePort.findAllByBuyerId(20L)).thenReturn(List.of(returned));

        mockMvc.perform(get("/api/v1/buyer/returns")
                        .with(authenticatedAs(user(7L, UserRole.BUYER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].orderId").value(100));

        verify(returnUseCasePort).findAllByBuyerId(20L);
    }

    @Test
    @DisplayName("crea una devolución solo si la orden pertenece al comprador")
    void createsReturnForOwnedOrder() throws Exception {
        Buyer buyer = buyer(20L, 7L);
        Order order = order(100L, 20L);
        Return returned = returnRequest(50L, 100L);
        when(buyerUseCasePort.findByUserId(7L)).thenReturn(Optional.of(buyer));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order);
        when(returnUseCasePort.createReturn(100L, "Producto defectuoso")).thenReturn(returned);

        mockMvc.perform(post("/api/v1/buyer/returns")
                        .with(authenticatedAs(user(7L, UserRole.BUYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":100,"reason":"Producto defectuoso"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    @DisplayName("rechaza crear una devolución sobre una orden ajena")
    void rejectsReturnForForeignOrder() throws Exception {
        Buyer buyer = buyer(20L, 7L);
        when(buyerUseCasePort.findByUserId(7L)).thenReturn(Optional.of(buyer));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order(100L, 99L));

        mockMvc.perform(post("/api/v1/buyer/returns")
                        .with(authenticatedAs(user(7L, UserRole.BUYER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":100,"reason":"Producto defectuoso"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(returnUseCasePort, never()).createReturn(100L, "Producto defectuoso");
    }

    private static User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("buyer@test.com")
                .fullName("Buyer")
                .password("$2a$10$hashed")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static Buyer buyer(Long id, Long userId) {
        return Buyer.builder()
                .id(id)
                .userId(userId)
                .primaryAddress("Calle 1")
                .commercialStatus(BuyerCommercialStatus.ACTIVE)
                .build();
    }

    private static Order order(Long id, Long buyerId) {
        return Order.builder()
                .id(id)
                .buyerId(buyerId)
                .build();
    }

    private static Return returnRequest(Long id, Long orderId) {
        return Return.builder()
                .id(id)
                .orderId(orderId)
                .reason("Producto defectuoso")
                .status(ReturnStatus.REQUESTED)
                .build();
    }

    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }
}
