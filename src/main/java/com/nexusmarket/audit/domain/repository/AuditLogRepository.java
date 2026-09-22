package com.nexusmarket.audit.domain.repository;

import com.nexusmarket.audit.domain.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByEntityName(String entityName);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}