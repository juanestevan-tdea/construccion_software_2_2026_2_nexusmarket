package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findAll();

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByEntityName(String entityName);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Optional<AuditLog> findById(String id);
}
