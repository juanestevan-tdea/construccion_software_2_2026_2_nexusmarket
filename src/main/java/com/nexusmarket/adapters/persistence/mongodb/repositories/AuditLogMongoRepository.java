package com.nexusmarket.adapters.persistence.mongodb.repositories;

import com.nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByUserId(Long userId);

    List<AuditLogDocument> findByEntityName(String entityName);

    List<AuditLogDocument> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
