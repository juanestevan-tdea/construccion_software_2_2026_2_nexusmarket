package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de respuesta para eventos de sesión.
 * No expone el identificador interno de MongoDB (_id), solo campos de negocio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEventDto {

    private String userEmail;
    private SessionEventType eventType;
    private String tokenId;      // jti del token (nullable)
    private String reason;       // motivo del rechazo/fallo (nullable)
    private String ipAddress;
    private String userAgent;
    private Integer tokenVersion;
    private Instant occurredAt;
}
