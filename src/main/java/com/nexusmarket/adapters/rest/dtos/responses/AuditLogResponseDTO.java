package com.nexusmarket.adapters.rest.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {

    private String id;
    private Long userId;
    private String performedBy;
    private String entityName;
    private String entityId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
