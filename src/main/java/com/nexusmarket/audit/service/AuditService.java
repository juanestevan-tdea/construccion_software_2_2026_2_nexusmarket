package com.nexusmarket.audit.service;

import com.nexusmarket.audit.domain.model.AuditLog;
import com.nexusmarket.audit.domain.repository.AuditLogRepository;
import com.nexusmarket.audit.dto.AuditLogResponse;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogResponse log(Long userId, String performedBy, String entityName, String entityId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .performedBy(performedBy)
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return AuditLogResponse.fromEntity(auditLogRepository.save(auditLog));
    }

    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAll().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    public List<AuditLogResponse> findByUser(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    public List<AuditLogResponse> findByEntity(String entityName) {
        return auditLogRepository.findByEntityName(entityName).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    public List<AuditLogResponse> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    public AuditLog getByIdOrThrow(String id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id));
    }

    public AuditLogResponse getAuditLogResponseById(String id) {
        return AuditLogResponse.fromEntity(getByIdOrThrow(id));
    }
}
