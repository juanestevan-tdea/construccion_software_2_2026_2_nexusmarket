package com.nexusmarket.users.service;

import com.nexusmarket.adapters.useCases.UserUseCaseImpl;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.UserManagementService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link UserUseCasePort} and domain services.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String RAW_PASSWORD = "secret123";
    private static final String HASHED_PASSWORD = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserUseCasePort userUseCase;

    @BeforeEach
    void setUp() {
        UserManagementService managementService = new UserManagementService(userRepositoryPort, passwordEncoder);
        userUseCase = new UserUseCaseImpl(managementService, userRepositoryPort);
    }

    // ---------- createUser: hashing (the security-critical case) ----------
    @Test
    @DisplayName("createUser_Buyer_Success_EncodesPasswordWithBCrypt")
    void createUser_buyer_success_encodesPasswordWithBCrypt() {
        when(userRepositoryPort.existsByEmail("buyer1@test.com")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userUseCase.createUser("buyer1@test.com", "Juan Perez", RAW_PASSWORD, UserRole.BUYER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        User persisted = userCaptor.getValue();

        // The raw password must never be persisted.
        assertThat(persisted.getPassword()).isNotEqualTo(RAW_PASSWORD);
        assertThat(persisted.getPassword()).isEqualTo(HASHED_PASSWORD);
        assertThat(persisted.getPassword()).startsWith("$2a$");

        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    // ---------- createUser: duplicate email ----------
    @Test
    @DisplayName("createUser_ThrowsDuplicateResourceException_WhenEmailExists")
    void createUser_throwsDuplicateResourceException_whenEmailExists() {
        when(userRepositoryPort.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userUseCase.createUser(
                "taken@test.com", "Juan Perez", RAW_PASSWORD, UserRole.BUYER))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepositoryPort, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ---------- createUser: default status and role ----------
    @Test
    @DisplayName("createUser_SetsStatusActive_ByDefault")
    void createUser_setsStatusActive_byDefault() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userUseCase.createUser("buyer1@test.com", "Juan Perez", RAW_PASSWORD, UserRole.BUYER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("createUser_SetsCorrectRole_ForBuyer")
    void createUser_setsCorrectRole_forBuyer() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userUseCase.createUser("buyer1@test.com", "Juan Perez", RAW_PASSWORD, UserRole.BUYER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        User persisted = userCaptor.getValue();
        assertThat(persisted.getRole()).isEqualTo(UserRole.BUYER);
        assertThat(persisted.getEmail()).isEqualTo("buyer1@test.com");
        assertThat(persisted.getFullName()).isEqualTo("Juan Perez");
    }

    @Test
    @DisplayName("createUser_SetsCorrectRole_ForSeller")
    void createUser_setsCorrectRole_forSeller() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userUseCase.createUser("seller1@test.com", "Tienda SA", RAW_PASSWORD, UserRole.SELLER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.SELLER);
    }

    // ---------- lookups ----------
    @Test
    @DisplayName("getUserByIdOrThrow_ThrowsResourceNotFoundException_WhenMissing")
    void getUserByIdOrThrow_throwsResourceNotFoundException_whenMissing() {
        when(userRepositoryPort.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userUseCase.getUserByIdOrThrow(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUserByEmailOrThrow_ThrowsResourceNotFoundException_WhenMissing")
    void getUserByEmailOrThrow_throwsResourceNotFoundException_whenMissing() {
        when(userRepositoryPort.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userUseCase.getUserByEmailOrThrow("ghost@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUserByIdOrThrow_ReturnsUser_WhenFound")
    void getUserByIdOrThrow_returnsUser_whenFound() {
        User user = User.builder()
                .id(1L)
                .email("buyer1@test.com")
                .fullName("Juan Perez")
                .password(HASHED_PASSWORD)
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));

        User result = userUseCase.getUserByIdOrThrow(1L);

        assertThat(result).isSameAs(user);
        assertThat(result.getPassword()).isEqualTo(HASHED_PASSWORD);
    }
}

