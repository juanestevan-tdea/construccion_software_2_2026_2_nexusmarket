package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.SessionEventDto;
import com.nexusmarket.domain.models.SessionEvent;
import lombok.experimental.UtilityClass;

/**
 * Traduce eventos de sesión de dominio al DTO de respuesta REST.
 */
@UtilityClass
public class SessionEventRestMapper {

    public SessionEventDto toResponseDTO(SessionEvent event) {
        if (event == null) {
            return null;
        }
        return SessionEventDto.builder()
                .userEmail(event.userEmail())
                .eventType(event.type())
                .tokenId(event.tokenId())
                .reason(event.reason())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .tokenVersion(event.tokenVersion())
                .occurredAt(event.occurredAt())
                .build();
    }
}
