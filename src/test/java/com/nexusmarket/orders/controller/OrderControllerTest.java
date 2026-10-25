package com.nexusmarket.orders.controller;

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

import com.nexusmarket.adapters.rest.controllers.buyer.OrderController;
import com.nexusmarket.domain.exceptions.InsufficientStockException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.RestAccessDeniedHandler;
import com.nexusmarket.infrastructure.security.RestAuthenticationEntryPoint;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import com.nexusmarket.domain.services.SessionAuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el aislamiento de datos entre compradores: un BUYER solo puede
 * consultar y operar sobre sus propias órdenes.
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderUseCasePort orderUseCasePort;

    @MockitoBean
    private BuyerUseCasePort buyerUseCasePort;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionAuditService sessionAuditService;

    private static User buyerUser() {
        return User.builder()
                .id(1L)
                .email("buyer@test.com")
                .fullName("Buyer")
                .password("$2a$10$hashed")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }

    private static Buyer buyer(Long id) {
        return Buyer.builder()
                .id(id)
                .userId(1L)
                .primaryAddress("Calle 1")
                .commercialStatus(BuyerCommercialStatus.ACTIVE)
                .build();
    }

    private static Order order(Long id, Long buyerId) {
        return Order.builder()
                .id(id)
                .buyerId(buyerId)
                .buyerEmail("buyer@test.com")
                .status(OrderStatus.CART)
                .build();
    }

    @Test
    @DisplayName("createOrder usa el comprador del token")
    void createOrderUsesAuthenticatedBuyer() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.createOrder(20L)).thenReturn(order(100L, 20L));

        mockMvc.perform(post("/api/v1/buyer/orders")
                        .with(authenticatedAs(buyerUser())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.buyerId").value(20));

        verify(orderUseCasePort).createOrder(20L);
    }

    @Test
    @DisplayName("getOrderById permite al dueño")
    void getOrderByIdAllowsOwner() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order(100L, 20L));

        mockMvc.perform(get("/api/v1/buyer/orders/100")
                        .with(authenticatedAs(buyerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("getOrderById devuelve 403 para una orden ajena")
    void getOrderByIdForbidsOtherBuyer() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order(100L, 99L));

        mockMvc.perform(get("/api/v1/buyer/orders/100")
                        .with(authenticatedAs(buyerUser())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("getAllOrders solo consulta las órdenes del comprador autenticado")
    void getAllOrdersScopesToAuthenticatedBuyer() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.findByBuyer(20L)).thenReturn(List.of(order(100L, 20L)));

        mockMvc.perform(get("/api/v1/buyer/orders")
                        .with(authenticatedAs(buyerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerId").value(20));

        verify(orderUseCasePort).findByBuyer(20L);
        verify(orderUseCasePort, never()).findAll();
    }

    @Test
    @DisplayName("getOrdersByBuyer devuelve 403 si el id no coincide con el token")
    void getOrdersByBuyerForbidsDifferentBuyer() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));

        mockMvc.perform(get("/api/v1/buyer/orders/buyer/99")
                        .with(authenticatedAs(buyerUser())))
                .andExpect(status().isForbidden());

        verify(orderUseCasePort, never()).findByBuyer(anyLong());
    }

    @Test
    @DisplayName("addItem devuelve 403 para una orden ajena")
    void addItemForbidsOtherBuyer() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order(100L, 99L));

        mockMvc.perform(post("/api/v1/buyer/orders/100/items")
                        .with(authenticatedAs(buyerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isForbidden());

        verify(orderUseCasePort, never()).addItem(any(), any(), any());
    }

    @Test
    @DisplayName("addItem devuelve 422 con ErrorResponse cuando no hay stock")
    void addItemReturns422WhenStockIsInsufficient() throws Exception {
        when(buyerUseCasePort.findByUserId(1L)).thenReturn(Optional.of(buyer(20L)));
        when(orderUseCasePort.getByIdOrThrow(100L)).thenReturn(order(100L, 20L));
        when(orderUseCasePort.addItem(100L, 5L, 2))
                .thenThrow(new InsufficientStockException(5L, 2, 0));

        mockMvc.perform(post("/api/v1/buyer/orders/100/items")
                        .with(authenticatedAs(buyerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":5,\"quantity\":2}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient stock for product '5': requested 2, available 0"))
                .andExpect(jsonPath("$.path").value("/api/v1/buyer/orders/100/items"));

        verify(orderUseCasePort).addItem(100L, 5L, 2);
    }
}
