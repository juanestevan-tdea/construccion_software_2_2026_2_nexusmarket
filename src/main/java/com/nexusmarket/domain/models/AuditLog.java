package com.nexusmarket.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    private String id;
    private Long userId;
    private String performedBy;
    private String entityName;
    private String entityId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
