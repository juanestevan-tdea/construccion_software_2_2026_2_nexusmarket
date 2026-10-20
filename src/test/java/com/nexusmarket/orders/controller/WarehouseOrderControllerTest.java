package com.nexusmarket.orders.controller;

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

import com.nexusmarket.adapters.rest.controllers.warehouse.WarehouseOrderController;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.OrderStatus;
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
 * Verifica que solo un WAREHOUSE_OPERATOR pueda ejecutar las transiciones
 * logísticas de una orden.
 */
@WebMvcTest(WarehouseOrderController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class WarehouseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderUseCasePort orderUseCasePort;

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

    private static Order order(Long id, OrderStatus status) {
        return Order.builder()
                .id(id)
                .buyerId(20L)
                .buyerEmail("buyer@test.com")
                .status(status)
                .build();
    }

    @Test
    @DisplayName("dispatch permite al operador de bodega")
    void dispatchAllowsWarehouseOperator() throws Exception {
        when(orderUseCasePort.dispatch(100L)).thenReturn(order(100L, OrderStatus.DISPATCHED));

        mockMvc.perform(patch("/api/v1/warehouse/orders/100/dispatch")
                        .with(authenticatedAs(user(3L, UserRole.WAREHOUSE_OPERATOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("DISPATCHED"));

        verify(orderUseCasePort).dispatch(100L);
    }

    @Test
    @DisplayName("deliver y finish permiten al operador de bodega")
    void deliverAndFinishAllowWarehouseOperator() throws Exception {
        when(orderUseCasePort.deliver(100L)).thenReturn(order(100L, OrderStatus.DELIVERED));
        when(orderUseCasePort.finish(100L)).thenReturn(order(100L, OrderStatus.FINISHED));

        mockMvc.perform(patch("/api/v1/warehouse/orders/100/deliver")
                        .with(authenticatedAs(user(3L, UserRole.WAREHOUSE_OPERATOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        mockMvc.perform(patch("/api/v1/warehouse/orders/100/finish")
                        .with(authenticatedAs(user(3L, UserRole.WAREHOUSE_OPERATOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));

        verify(orderUseCasePort).deliver(100L);
        verify(orderUseCasePort).finish(100L);
    }

    @Test
    @DisplayName("un comprador no puede despachar su propia orden")
    void buyerCannotDispatchOrder() throws Exception {
        mockMvc.perform(patch("/api/v1/warehouse/orders/100/dispatch")
                        .with(authenticatedAs(user(1L, UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(orderUseCasePort, never()).dispatch(100L);
    }

    @Test
    @DisplayName("dispatch devuelve 404 si la orden no existe")
    void dispatchReturns404WhenOrderMissing() throws Exception {
        when(orderUseCasePort.dispatch(999L)).thenThrow(new ResourceNotFoundException("Order", 999L));

        mockMvc.perform(patch("/api/v1/warehouse/orders/999/dispatch")
                        .with(authenticatedAs(user(3L, UserRole.WAREHOUSE_OPERATOR))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
