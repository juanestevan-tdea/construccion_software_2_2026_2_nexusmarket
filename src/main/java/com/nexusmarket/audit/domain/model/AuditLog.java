package com.nexusmarket.audit.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    private Long userId;
    private String performedBy;
    private String entityName;
    private String entityId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}