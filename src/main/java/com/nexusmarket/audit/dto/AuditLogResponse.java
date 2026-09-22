package com.nexusmarket.audit.dto;

import com.nexusmarket.audit.domain.model.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String id;
    private Long userId;
    private String performedBy;
    private String entityName;
    private String entityId;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public static AuditLogResponse fromEntity(AuditLog log) {
        if (log == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .performedBy(log.getPerformedBy())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
