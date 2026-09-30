package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.AuditLogResponseDTO;
import com.nexusmarket.domain.models.AuditLog;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditLogRestMapper {

    public AuditLogResponseDTO toResponseDTO(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        return AuditLogResponseDTO.builder()
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
}
