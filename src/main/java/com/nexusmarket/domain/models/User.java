package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String email;
    private String fullName;
    private String password;
    private UserRole role;
    private UserStatus status;
    @Builder.Default
    private Integer tokenVersion = 0;
    private Instant lastLogoutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Invalida todas las sesiones activas del usuario incrementando el tokenVersion
     * y registrando la fecha de logout. Cualquier JWT emitido con un ver anterior
     * será rechazado en la siguiente validación.
     */
    public void invalidateSessions() {
        this.tokenVersion = (this.tokenVersion == null ? 0 : this.tokenVersion) + 1;
        this.lastLogoutAt = Instant.now();
    }
}
