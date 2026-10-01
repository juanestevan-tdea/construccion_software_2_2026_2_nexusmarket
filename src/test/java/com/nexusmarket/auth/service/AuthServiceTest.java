package com.nexusmarket.auth.service;

import com.nexusmarket.auth.dto.AuthResponse;
import com.nexusmarket.auth.dto.LoginRequest;
import com.nexusmarket.auth.dto.RegisterRequest;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.SellerUseCasePort;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import com.nexusmarket.security.JwtService;
import com.nexusmarket.security.UserDetailsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link AuthService}.
 *
 * <p>
 * No Spring context is started: {@code @ExtendWith(MockitoExtension.class)}
 * builds the class under test from mocks. Note that {@code AuthService}
 * delegates user creation (and therefore password hashing) to
 * {@link UserUseCasePort}, so the hashing itself is verified in
 * {@code UserServiceTest}, not here.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long ONE_DAY_MS = 86400000L;

    @Mock
    private UserUseCasePort userUseCasePort;

    @Mock
    private BuyerUseCasePort buyerUseCasePort;

    @Mock
    private SellerUseCasePort sellerUseCasePort;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    /**
     * {@code jwtExpiration} is injected as a field by {@code @Value}, so in a
     * pure Mockito test it would stay 0. It is set explicitly to keep
     * {@code expiresIn} assertions meaningful.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", ONE_DAY_MS);
    }

    // ---------- helpers ----------
    private static User persistedUser(Long id, String email, UserRole role) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName("Juan Perez")
                .password("$2a$10$hashed")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static RegisterRequest buyerRequest() {
        return RegisterRequest.builder()
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password("secret123")
                .role(UserRole.BUYER)
                .primaryAddress("Calle 123 #45-67")
                .build();
    }

    private static RegisterRequest sellerRequest() {
        return RegisterRequest.builder()
                .email("seller1@test.com")
                .fullName("Tienda SA")
                .password("secret123")
                .role(UserRole.SELLER)
                .taxId("900123456-7")
                .companyName("Tienda Nexus")
                .build();
    }

    // ---------- register: happy paths ----------
    @Test
    @DisplayName("register_Buyer_Success")
    void register_buyer_success() {
        RegisterRequest request = buyerRequest();
        User saved = persistedUser(1L, "buyer1@test.com", UserRole.BUYER);

        when(userUseCasePort.createUser("buyer1@test.com", "Juan Perez", "secret123", UserRole.BUYER))
                .thenReturn(saved);
        when(jwtService.generateToken(any(UserDetailsAdapter.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(ONE_DAY_MS);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("buyer1@test.com");
        assertThat(response.getRole()).isEqualTo(UserRole.BUYER);

        verify(buyerUseCasePort).createBuyer(1L, "Calle 123 #45-67");
        verify(sellerUseCasePort, never()).createSeller(any(), any(), any());
        verify(jwtService).generateToken(any(UserDetailsAdapter.class));
    }

    @Test
    @DisplayName("register_Seller_Success")
    void register_seller_success() {
        RegisterRequest request = sellerRequest();
        User saved = persistedUser(2L, "seller1@test.com", UserRole.SELLER);

        when(userUseCasePort.createUser("seller1@test.com", "Tienda SA", "secret123", UserRole.SELLER))
                .thenReturn(saved);
        when(jwtService.generateToken(any(UserDetailsAdapter.class))).thenReturn("fake.jwt.token.seller");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("fake.jwt.token.seller");
        assertThat(response.getRole()).isEqualTo(UserRole.SELLER);
        assertThat(response.getEmail()).isEqualTo("seller1@test.com");

        verify(sellerUseCasePort).createSeller(2L, "900123456-7", "Tienda Nexus");
        verify(buyerUseCasePort, never()).createBuyer(any(), any());
    }

    // ---------- register: forbidden roles ----------
    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenRoleIsAdmin")
    void register_throwsBusinessRuleException_whenRoleIsAdmin() {
        RegisterRequest request = buyerRequest();
        request.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Self-registration is only allowed for BUYER and SELLER roles");

        verifyNoInteractions(userUseCasePort, buyerUseCasePort, sellerUseCasePort, jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenRoleIsWarehouseOperator")
    void register_throwsBusinessRuleException_whenRoleIsWarehouseOperator() {
        RegisterRequest request = buyerRequest();
        request.setRole(UserRole.WAREHOUSE_OPERATOR);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Self-registration is only allowed for BUYER and SELLER roles");

        verifyNoInteractions(userUseCasePort, buyerUseCasePort, sellerUseCasePort, jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenRoleIsSupervisor")
    void register_throwsBusinessRuleException_whenRoleIsSupervisor() {
        RegisterRequest request = buyerRequest();
        request.setRole(UserRole.SUPERVISOR);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Self-registration is only allowed for BUYER and SELLER roles");

        verifyNoInteractions(userUseCasePort, buyerUseCasePort, sellerUseCasePort, jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenRoleIsNull")
    void register_throwsBusinessRuleException_whenRoleIsNull() {
        RegisterRequest request = buyerRequest();
        request.setRole(null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Self-registration is only allowed for BUYER and SELLER roles");

        verifyNoInteractions(userUseCasePort, buyerUseCasePort, sellerUseCasePort, jwtService);
    }

    // ---------- register: missing conditional fields ----------
    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenBuyerWithoutPrimaryAddress")
    void register_throwsBusinessRuleException_whenBuyerWithoutPrimaryAddress() {
        RegisterRequest request = buyerRequest();
        request.setPrimaryAddress(null);

        User saved = persistedUser(1L, "buyer1@test.com", UserRole.BUYER);
        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), eq(UserRole.BUYER)))
                .thenReturn(saved);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("primaryAddress is required for BUYER registration");

        verify(buyerUseCasePort, never()).createBuyer(any(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenBuyerWithBlankPrimaryAddress")
    void register_throwsBusinessRuleException_whenBuyerWithBlankPrimaryAddress() {
        RegisterRequest request = buyerRequest();
        request.setPrimaryAddress("   ");

        User saved = persistedUser(1L, "buyer1@test.com", UserRole.BUYER);
        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), eq(UserRole.BUYER)))
                .thenReturn(saved);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("primaryAddress is required for BUYER registration");

        verify(buyerUseCasePort, never()).createBuyer(any(), any());
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenSellerWithoutTaxId")
    void register_throwsBusinessRuleException_whenSellerWithoutTaxId() {
        RegisterRequest request = sellerRequest();
        request.setTaxId(null);

        User saved = persistedUser(2L, "seller1@test.com", UserRole.SELLER);
        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), eq(UserRole.SELLER)))
                .thenReturn(saved);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("taxId is required for SELLER registration");

        verify(sellerUseCasePort, never()).createSeller(any(), any(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenSellerWithoutCompanyName")
    void register_throwsBusinessRuleException_whenSellerWithoutCompanyName() {
        RegisterRequest request = sellerRequest();
        request.setCompanyName(null);

        User saved = persistedUser(2L, "seller1@test.com", UserRole.SELLER);
        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), eq(UserRole.SELLER)))
                .thenReturn(saved);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("companyName is required for SELLER registration");

        verify(sellerUseCasePort, never()).createSeller(any(), any(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register_ThrowsBusinessRuleException_WhenSellerWithBlankCompanyName")
    void register_throwsBusinessRuleException_whenSellerWithBlankCompanyName() {
        RegisterRequest request = sellerRequest();
        request.setCompanyName("  ");

        User saved = persistedUser(2L, "seller1@test.com", UserRole.SELLER);
        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), eq(UserRole.SELLER)))
                .thenReturn(saved);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("companyName is required for SELLER registration");

        verify(sellerUseCasePort, never()).createSeller(any(), any(), any());
    }

    // ---------- register: delegated duplicate detection ----------
    @Test
    @DisplayName("register_PropagatesDuplicateResourceException_FromUserService")
    void register_propagatesDuplicateResourceException_fromUserService() {
        RegisterRequest request = buyerRequest();

        when(userUseCasePort.createUser(anyString(), anyString(), anyString(), any(UserRole.class)))
                .thenThrow(new DuplicateResourceException("User", "email", "buyer1@test.com"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(buyerUseCasePort, never()).createBuyer(any(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register_CallsUserServiceWithRequestValues")
    void register_callsUserServiceWithRequestValues() {
        RegisterRequest request = buyerRequest();
        User saved = persistedUser(1L, "buyer1@test.com", UserRole.BUYER);

        when(userUseCasePort.createUser("buyer1@test.com", "Juan Perez", "secret123", UserRole.BUYER))
                .thenReturn(saved);
        when(jwtService.generateToken(any(UserDetailsAdapter.class))).thenReturn("fake.jwt.token");

        authService.register(request);

        verify(userUseCasePort).createUser("buyer1@test.com", "Juan Perez", "secret123", UserRole.BUYER);
    }

    // ---------- login ----------
    @Test
    @DisplayName("login_Success")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .email("buyer1@test.com")
                .password("secret123")
                .build();

        User user = persistedUser(1L, "buyer1@test.com", UserRole.BUYER);
        UserDetailsAdapter principal = new UserDetailsAdapter(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("fake.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(ONE_DAY_MS);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("buyer1@test.com");
        assertThat(response.getRole()).isEqualTo(UserRole.BUYER);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(principal);
    }

    @Test
    @DisplayName("login_ThrowsBadCredentialsException_WhenPasswordWrong")
    void login_throwsBadCredentialsException_whenPasswordWrong() {
        LoginRequest request = LoginRequest.builder()
                .email("buyer1@test.com")
                .password("wrong-password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService, userUseCasePort, buyerUseCasePort, sellerUseCasePort);
    }
}
