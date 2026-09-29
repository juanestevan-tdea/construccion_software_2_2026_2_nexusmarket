package com.nexusmarket.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.security.CustomUserDetailsService;
import com.nexusmarket.security.JwtAuthenticationFilter;
import com.nexusmarket.security.JwtService;
import com.nexusmarket.security.RestAccessDeniedHandler;
import com.nexusmarket.security.RestAuthenticationEntryPoint;
import com.nexusmarket.security.UserDetailsAdapter;
import com.nexusmarket.config.SecurityConfig;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.model.UserStatus;
import com.nexusmarket.users.dto.request.UserCreateRequest;
import com.nexusmarket.users.dto.response.UserResponse;
import com.nexusmarket.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

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
 * MVC slice tests for {@link UserController} exercising the <b>real</b>
 * security chain.
 *
 * <p>
 * Unlike {@code AuthControllerTest}, this class imports the production security
 * configuration on purpose, so the assertions genuinely cover authentication
 * (401) and authorization (403) behaviour, including the JSON bodies produced
 * by {@link RestAuthenticationEntryPoint} and
 * {@link RestAccessDeniedHandler}.</p>
 *
 * <p>
 * {@code @WithMockUser} is not used for {@code /api/users/me}: it injects a
 * plain Spring {@code User} as the principal, which is not a
 * {@link UserDetailsAdapter}, and the controller would NPE on
 * {@code principal.getUser()}. A custom {@link RequestPostProcessor} places a
 * real {@code UserDetailsAdapter} in the context instead.</p>
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class,
    CustomUserDetailsService.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- helpers ----------
    private static User sampleUser(Long id, String email, String fullName, UserRole role) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .password("$2a$10$hashedpassword")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * Puts a genuine {@link UserDetailsAdapter} principal in the request through the
     * official {@code spring-security-test} post-processor.
     *
     * <p>Hand-rolling a {@code SecurityContext} and attaching it as a request
     * attribute does not work: the security filter chain reads the context from
     * {@code SecurityContextHolder}/the context repository and overwrites whatever
     * the attribute holds. Passing the adapter as a {@code UserDetails} here is what
     * makes {@code @AuthenticationPrincipal UserDetailsAdapter} bind correctly.</p>
     */
    private static RequestPostProcessor authenticatedAs(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new UserDetailsAdapter(user));
    }

    // ---------- GET /api/users/me ----------
    @Test
    @DisplayName("getCurrentUser_Returns200_WhenAuthenticated")
    void getCurrentUser_returns200_whenAuthenticated() throws Exception {
        User user = sampleUser(1L, "test@test.com", "Test User", UserRole.BUYER);

        mockMvc.perform(get("/api/users/me")
                .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("getCurrentUser_Returns401WithErrorResponse_WhenNoToken")
    void getCurrentUser_returns401WithErrorResponse_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/users/me"));
    }

    // ---------- GET /api/users/{id} ----------
    @Test
    @DisplayName("getUserById_Returns200_WhenAdmin")
    void getUserById_returns200_whenAdmin() throws Exception {
        User user = sampleUser(1L, "target@test.com", "Target User", UserRole.SELLER);
        when(userService.getUserByIdOrThrow(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                .with(authenticatedAs(sampleUser(99L, "admin@test.com", "Admin", UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("target@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(userService).getUserByIdOrThrow(1L);
    }

    @Test
    @DisplayName("getUserById_Returns403WithErrorResponse_WhenBuyer")
    void getUserById_returns403WithErrorResponse_whenBuyer() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .with(authenticatedAs(sampleUser(2L, "buyer@test.com", "Buyer", UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/users/1"));

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("getUserById_Returns403WithErrorResponse_WhenSeller")
    void getUserById_returns403WithErrorResponse_whenSeller() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .with(authenticatedAs(sampleUser(3L, "seller@test.com", "Seller", UserRole.SELLER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/api/users/1"));

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("getUserById_Returns404_WhenAdminAndUserMissing")
    void getUserById_returns404_whenAdminAndUserMissing() throws Exception {
        when(userService.getUserByIdOrThrow(404L))
                .thenThrow(new ResourceNotFoundException("User", 404L));

        mockMvc.perform(get("/api/users/404")
                .with(authenticatedAs(sampleUser(99L, "admin@test.com", "Admin", UserRole.ADMIN))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(userService).getUserByIdOrThrow(404L);
    }

    // ---------- POST /api/users ----------
    @Test
    @DisplayName("createUser_Returns201_WhenAdminAndValidDto")
    void createUser_returns201_whenAdminAndValidDto() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@test.com")
                .fullName("New User")
                .password("secret123")
                .role(UserRole.SELLER)
                .build();

        UserResponse response = UserResponse.builder()
                .id(10L)
                .email("newuser@test.com")
                .fullName("New User")
                .role(UserRole.SELLER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .with(authenticatedAs(sampleUser(99L, "admin@test.com", "Admin", UserRole.ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("createUser_Returns403_WhenBuyer")
    void createUser_returns403_whenBuyer() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@test.com")
                .fullName("New User")
                .password("secret123")
                .role(UserRole.SELLER)
                .build();

        mockMvc.perform(post("/api/users")
                .with(authenticatedAs(sampleUser(2L, "buyer@test.com", "Buyer", UserRole.BUYER)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(userService, never()).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("createUser_Returns403_WhenSeller")
    void createUser_returns403_whenSeller() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@test.com")
                .fullName("New User")
                .password("secret123")
                .role(UserRole.SELLER)
                .build();

        mockMvc.perform(post("/api/users")
                .with(authenticatedAs(sampleUser(3L, "seller@test.com", "Seller", UserRole.SELLER)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(userService, never()).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("createUser_Returns400_WhenDtoInvalid")
    void createUser_returns400_whenDtoInvalid() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("not-an-email")
                .fullName("")
                .password("123")
                .role(null)
                .build();

        mockMvc.perform(post("/api/users")
                .with(authenticatedAs(sampleUser(99L, "admin@test.com", "Admin", UserRole.ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("createUser_Returns401_WhenNoToken")
    void createUser_returns401_whenNoToken() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("newuser@test.com")
                .fullName("New User")
                .password("secret123")
                .role(UserRole.SELLER)
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(userService, never()).createUser(any(UserCreateRequest.class));
    }

    // ---------- WAREHOUSE_OPERATOR / SUPERVISOR boundary ----------
    @Test
    @DisplayName("getUserById_Returns403_WhenWarehouseOperator")
    void getUserById_returns403_whenWarehouseOperator() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .with(authenticatedAs(sampleUser(4L, "warehouse@test.com", "Warehouse",
                        UserRole.WAREHOUSE_OPERATOR))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("getUserById_Returns403_WhenSupervisor")
    void getUserById_returns403_whenSupervisor() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .with(authenticatedAs(sampleUser(5L, "supervisor@test.com", "Supervisor",
                        UserRole.SUPERVISOR))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    // ---------- @WithMockUser control test ----------
    @Test
    @DisplayName("getUserById_Returns403_WhenMockUserHasNoRole")
    void getUserById_returns403_whenMockUserHasNoRole() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("plain@test.com")))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserByIdOrThrow(anyLong());
    }

    @Test
    @DisplayName("getUsersByRole_Returns200_WhenAdmin")
    void getUsersByRole_returns200_whenAdmin() throws Exception {
        when(userService.findByRole(UserRole.SELLER))
                .thenReturn(List.of(sampleUser(1L, "s@test.com", "Seller", UserRole.SELLER)));

        mockMvc.perform(get("/api/users/role/SELLER")
                        .with(authenticatedAs(sampleUser(99L, "admin@test.com", "Admin", UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("s@test.com"));

        verify(userService).findByRole(UserRole.SELLER);
    }

    @Test
    @DisplayName("getAllUsers_Returns403_WhenBuyer")
    void getAllUsers_returns403_whenBuyer() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(authenticatedAs(sampleUser(2L, "buyer@test.com", "Buyer", UserRole.BUYER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(userService, never()).findAll();
    }
}
