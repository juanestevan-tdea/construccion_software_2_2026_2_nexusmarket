package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.SessionEventType;

import java.time.Instant;

/**
 * Evento de sesión de seguridad.
 *
 * <p>Modelo de dominio independiente de MongoDB y de HTTP. La persistencia se
 * resuelve mediante {@code SessionAuditPort}.</p>
 */
public record SessionEvent(
        String userEmail,
        SessionEventType type,
        String tokenId,
        String reason,
        String ipAddress,
        String userAgent,
        Integer tokenVersion,
        Instant occurredAt
) {
}
