package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.valueobjects.SessionEventType;

import java.time.Instant;

/**
 * Puerto de salida para registrar eventos de sesión de auditoría.
 * La implementación concreta (MongoDB) vive en el adaptador;
 * el dominio solo conoce este contrato.
 */
public interface SessionAuditPort {

    /**
     * Datos de un evento de sesión. Inmutable, sin depender de tecnología de persistencia.
     */
    record SessionEventData(
            String userEmail,
            SessionEventType type,
            String tokenId,      // jti del token (nullable)
            String reason,       // motivo del rechazo/fallo (nullable)
            String ipAddress,
            String userAgent,
            Integer tokenVersion,
            Instant occurredAt
    ) {
    }

    /**
     * Registra un evento de sesión.
     * La implementación debe tolerar fallos de persistencia sin romper el flujo de negocio.
     *
     * @param event datos del evento
     */
    void record(SessionEventData event);
}
