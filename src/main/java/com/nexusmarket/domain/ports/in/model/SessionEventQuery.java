package com.nexusmarket.domain.ports.in.model;

import com.nexusmarket.domain.valueobjects.SessionEventType;

import java.time.Instant;

/**
 * Criterios de consulta de eventos de sesión.
 *
 * <p>Los filtros son opcionales. La consulta no conoce MongoDB ni Spring Data.</p>
 */
public record SessionEventQuery(
        String email,
        SessionEventType type,
        Instant from,
        Instant to,
        int page,
        int size
) {

    public SessionEventQuery {
        email = email == null || email.isBlank() ? null : email.trim();
    }
}
