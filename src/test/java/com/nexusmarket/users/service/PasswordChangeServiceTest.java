package com.nexusmarket.users.service;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.PasswordHasherPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.PasswordChangeService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    private PasswordChangeService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new PasswordChangeService(userRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("cambia la contraseña, la cifra e invalida las sesiones")
    void changePassword_updatesHashAndInvalidatesSessions() {
        User user = user(1L, 3);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("OldPass123", "old-hash")).thenReturn(true);
        when(passwordHasherPort.hash("NewPass123")).thenReturn("new-hash");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.changePassword(1L, "OldPass123", "NewPass123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("new-hash");
        assertThat(captor.getValue().getTokenVersion()).isEqualTo(4);
        assertThat(result).isSameAs(user);
    }

    @Test
    @DisplayName("rechaza la contraseña actual incorrecta sin guardar")
    void changePassword_rejectsWrongCurrentPassword() {
        User user = user(1L, 0);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("WrongPass1", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(1L, "WrongPass1", "NewPass123"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("actual");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("rechaza una contraseña nueva que no cumple la política")
    void changePassword_rejectsWeakNewPassword() {
        User user = user(1L, 0);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("OldPass123", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(1L, "OldPass123", "short"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("8 caracteres");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException si el usuario no existe")
    void changePassword_userNotFound() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(99L, "OldPass123", "NewPass123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User user(Long id, int tokenVersion) {
        return User.builder()
                .id(id)
                .email("buyer@test.com")
                .fullName("Buyer")
                .password("old-hash")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .tokenVersion(tokenVersion)
                .build();
    }
}
