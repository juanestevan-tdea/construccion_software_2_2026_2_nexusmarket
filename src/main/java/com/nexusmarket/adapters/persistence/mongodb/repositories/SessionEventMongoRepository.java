package com.nexusmarket.adapters.persistence.mongodb.repositories;

import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

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

    /**
     * Consulta paginada y filtrada de eventos de sesión.
     * Los parámetros opcionales (null) se ignoran en el filtro.
     */
    @Query(value = "{'userEmail': ?0, 'eventType': ?1, 'occurredAt': {'$gte': ?2, '$lte': ?3}}")
    Page<SessionEventDocument> findByFilters(String userEmail, SessionEventType eventType,
                                             Instant start, Instant end, Pageable pageable);
}
