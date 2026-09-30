package com.nexusmarket.adapters.persistence.mongodb.adapters;

import com.nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import com.nexusmarket.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import com.nexusmarket.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import com.nexusmarket.domain.models.AuditLog;
import com.nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository auditLogMongoRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditLogDocument document = AuditLogMongoMapper.toDocument(auditLog);
        AuditLogDocument saved = auditLogMongoRepository.save(document);
        return AuditLogMongoMapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogMongoRepository.findAll().stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return auditLogMongoRepository.findByUserId(userId).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByEntityName(String entityName) {
        return auditLogMongoRepository.findByEntityName(entityName).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return auditLogMongoRepository.findByTimestampBetween(start, end).stream()
                .map(AuditLogMongoMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AuditLog> findById(String id) {
        return auditLogMongoRepository.findById(id)
                .map(AuditLogMongoMapper::toDomain);
    }
}
