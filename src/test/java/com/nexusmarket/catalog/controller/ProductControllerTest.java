package com.nexusmarket.catalog.controller;

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

import com.nexusmarket.adapters.rest.controllers.seller.ProductController;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.seller.ProductUseCasePort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.valueobjects.ProductType;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.infrastructure.security.CustomUserDetailsService;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.RestAccessDeniedHandler;
import com.nexusmarket.infrastructure.security.RestAuthenticationEntryPoint;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import com.nexusmarket.domain.services.SessionAuditService;

import java.util.Optional;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el vendedor autenticado no pueda suplantar a otro vendedor y que
 * solo pueda modificar productos de su propiedad.
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductUseCasePort productUseCasePort;

    @MockitoBean
    private SellerRepositoryPort sellerRepositoryPort;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionAuditService sessionAuditService;

    private static User sellerUser() {
        return User.builder()
                .id(1L)
                .email("seller@test.com")
                .fullName("Seller")
                .password("$2a$10$hashed")
                .role(UserRole.SELLER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }

    private static Seller seller(Long id, Long userId) {
        return Seller.builder().id(id).userId(userId).taxId("TAX-" + id)
                .companyName("Company " + id).active(true).build();
    }

    private static Product product(Long id, Long sellerId) {
        return Product.builder()
                .id(id)
                .sku("SKU-" + id)
                .name("Product " + id)
                .description("desc")
                .price(new BigDecimal("100.00"))
                .type(ProductType.PHYSICAL)
                .active(true)
                .sellerId(sellerId)
                .categoryId(2L)
                .build();
    }

    @Test
    @DisplayName("createProduct usa el vendedor del token e ignora un sellerId suplantado")
    void createProductUsesAuthenticatedSeller() throws Exception {
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(productUseCasePort.createProduct(
                eq("SKU-1"), eq("Phone"), eq("desc"), eq(new BigDecimal("100.00")),
                eq(ProductType.PHYSICAL), eq(10L), eq(2L)))
                .thenReturn(product(50L, 10L));

        String body = """
                {"sku":"SKU-1","name":"Phone","description":"desc","price":100.00,
                 "type":"PHYSICAL","sellerId":999,"categoryId":2}
                """;

        mockMvc.perform(post("/api/v1/seller/products")
                        .with(authenticatedAs(sellerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").value(10))
                .andExpect(jsonPath("$.sku").value("SKU-50"));

        verify(productUseCasePort).createProduct(
                "SKU-1", "Phone", "desc", new BigDecimal("100.00"),
                ProductType.PHYSICAL, 10L, 2L);
    }

    @Test
    @DisplayName("createProduct devuelve 404 si el usuario no tiene perfil de vendedor")
    void createProductReturns404WhenSellerProfileMissing() throws Exception {
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/seller/products")
                        .with(authenticatedAs(sellerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","name":"Phone","price":100.00,"type":"PHYSICAL","categoryId":2}
                                """))
                .andExpect(status().isNotFound());

        verify(productUseCasePort, never()).createProduct(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateProduct permite al dueño del producto")
    void updateProductAllowsOwner() throws Exception {
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(productUseCasePort.getProductByIdOrThrow(5L)).thenReturn(product(5L, 10L));
        when(productUseCasePort.updateProduct(eq(5L), any(), any(), any(), any(), any()))
                .thenReturn(product(5L, 10L));

        mockMvc.perform(patch("/api/v1/seller/products/5")
                        .with(authenticatedAs(sellerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(productUseCasePort).updateProduct(eq(5L), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateProduct devuelve 403 si el producto es de otro vendedor")
    void updateProductForbidsOtherSeller() throws Exception {
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(productUseCasePort.getProductByIdOrThrow(5L)).thenReturn(product(5L, 99L));

        mockMvc.perform(patch("/api/v1/seller/products/5")
                        .with(authenticatedAs(sellerUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijacked\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(productUseCasePort, never()).updateProduct(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("deleteProduct devuelve 403 si el producto es de otro vendedor")
    void deleteProductForbidsOtherSeller() throws Exception {
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(seller(10L, 1L)));
        when(productUseCasePort.getProductByIdOrThrow(5L)).thenReturn(product(5L, 99L));

        mockMvc.perform(delete("/api/v1/seller/products/5")
                        .with(authenticatedAs(sellerUser())))
                .andExpect(status().isForbidden());

        verify(productUseCasePort, never()).deactivateProduct(any());
    }
}
