package com.nexusmarket.adapters.persistence.mongodb.mappers;

import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.domain.models.SessionEvent;
import lombok.experimental.UtilityClass;

/**
 * Mapper entre el modelo de dominio {@link SessionEvent} y el documento MongoDB
 * ({@link SessionEventDocument}).
 */
@UtilityClass
public class SessionEventMongoMapper {

    public SessionEventDocument toDocument(SessionEvent data) {
        if (data == null) {
            return null;
        }
        return SessionEventDocument.builder()
                .userEmail(data.userEmail())
                .eventType(data.type())
                .tokenId(data.tokenId())
                .reason(data.reason())
                .ipAddress(data.ipAddress())
                .userAgent(data.userAgent())
                .tokenVersion(data.tokenVersion())
                .occurredAt(data.occurredAt())
                .build();
    }

    public SessionEvent toDomain(SessionEventDocument document) {
        if (document == null) {
            return null;
        }
        return new SessionEvent(
                document.getUserEmail(),
                document.getEventType(),
                document.getTokenId(),
                document.getReason(),
                document.getIpAddress(),
                document.getUserAgent(),
                document.getTokenVersion(),
                document.getOccurredAt()
        );
    }
}
