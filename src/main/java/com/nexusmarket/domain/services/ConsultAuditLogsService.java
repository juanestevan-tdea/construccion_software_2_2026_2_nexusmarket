package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.AuditLog;
import com.nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultAuditLogsService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public List<AuditLog> findAll() {
        return auditLogRepositoryPort.findAll();
    }

    public List<AuditLog> findByUser(Long userId) {
        return auditLogRepositoryPort.findByUserId(userId);
    }

    public List<AuditLog> findByEntity(String entityName) {
        return auditLogRepositoryPort.findByEntityName(entityName);
    }

    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepositoryPort.findByTimestampBetween(start, end);
    }

    public AuditLog getByIdOrThrow(String id) {
        return auditLogRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id));
    }
}

