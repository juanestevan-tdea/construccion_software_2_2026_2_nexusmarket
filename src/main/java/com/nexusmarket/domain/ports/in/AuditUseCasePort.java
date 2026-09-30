package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditUseCasePort {

    AuditLog log(Long userId, String performedBy, String entityName, String entityId, String action, String details);

    List<AuditLog> findAll();

    List<AuditLog> findByUser(Long userId);

    List<AuditLog> findByEntity(String entityName);

    List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end);

    AuditLog getByIdOrThrow(String id);
}
