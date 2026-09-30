package com.nexusmarket.adapters.persistence.mongodb.mappers;

import com.nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import com.nexusmarket.domain.models.AuditLog;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditLogMongoMapper {

    public AuditLogDocument toDocument(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        return AuditLogDocument.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .performedBy(domain.getPerformedBy())
                .entityName(domain.getEntityName())
                .entityId(domain.getEntityId())
                .action(domain.getAction())
                .details(domain.getDetails())
                .timestamp(domain.getTimestamp())
                .build();
    }

    public AuditLog toDomain(AuditLogDocument document) {
        if (document == null) {
            return null;
        }
        return AuditLog.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .performedBy(document.getPerformedBy())
                .entityName(document.getEntityName())
                .entityId(document.getEntityId())
                .action(document.getAction())
                .details(document.getDetails())
                .timestamp(document.getTimestamp())
                .build();
    }
}
