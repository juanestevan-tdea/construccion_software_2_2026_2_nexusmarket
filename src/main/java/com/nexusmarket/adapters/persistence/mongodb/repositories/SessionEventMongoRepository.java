package com.nexusmarket.adapters.persistence.mongodb.repositories;

import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio Mongo para eventos de sesión.
 * Spring Data MongoDB proporciona automáticamente la implementación de las interfaces
 * que extienden {@link MongoRepository}.
 */
public interface SessionEventMongoRepository extends MongoRepository<SessionEventDocument, String> {

    List<SessionEventDocument> findByUserEmail(String userEmail);

    List<SessionEventDocument> findByEventType(SessionEventType eventType);

    List<SessionEventDocument> findByOccurredAtBetween(Instant start, Instant end);

    Page<SessionEventDocument> findByOccurredAtBetween(Instant start, Instant end, Pageable pageable);

    Page<SessionEventDocument> findByUserEmailAndOccurredAtBetween(String userEmail, Instant start,
            Instant end, Pageable pageable);

    Page<SessionEventDocument> findByEventTypeAndOccurredAtBetween(SessionEventType eventType,
            Instant start, Instant end, Pageable pageable);

    Page<SessionEventDocument> findByUserEmailAndEventTypeAndOccurredAtBetween(String userEmail,
            SessionEventType eventType, Instant start, Instant end, Pageable pageable);
}
