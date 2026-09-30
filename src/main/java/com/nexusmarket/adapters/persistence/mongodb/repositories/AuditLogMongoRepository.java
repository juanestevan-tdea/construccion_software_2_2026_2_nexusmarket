package com.nexusmarket.adapters.persistence.mongodb.repositories;

import com.nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByUserId(Long userId);

    List<AuditLogDocument> findByEntityName(String entityName);

    List<AuditLogDocument> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
