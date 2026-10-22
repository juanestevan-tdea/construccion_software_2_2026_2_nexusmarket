package com.nexusmarket.adapters.persistence.mongodb.adapters;

import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.adapters.persistence.mongodb.mappers.SessionEventMongoMapper;
import com.nexusmarket.adapters.persistence.mongodb.repositories.SessionEventMongoRepository;
import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Adaptador de persistencia MongoDB para el puerto de auditoría de sesiones.
 * Implementa {@link SessionAuditPort} usando Spring Data MongoDB.
 *
 * <p>La tolerancia a fallos de escritura es responsabilidad del servicio de
 * aplicación, que es el único que decide si la auditoría puede bloquear el
 * login/logout/cambio de contraseña. Este adaptador no duplica ese manejo de
 * errores ni registra el mismo fallo dos veces.</p>
 */
@Repository
@RequiredArgsConstructor
public class SessionAuditMongoAdapter implements SessionAuditPort {

    // MongoDB/BSON no puede representar Instant.MIN/MAX: los límites abiertos se
    // sustituyen por un rango seguro para eventos de auditoría modernos.
    private static final Instant EARLIEST_EVENT = Instant.EPOCH;
    private static final Instant LATEST_EVENT = Instant.parse("9999-12-31T23:59:59Z");

    private final SessionEventMongoRepository sessionEventMongoRepository;

    @Override
    public void record(SessionEvent event) {
        if (event == null) {
            return;
        }
        SessionEventDocument document = SessionEventMongoMapper.toDocument(event);
        sessionEventMongoRepository.save(document);
    }

    @Override
    public PageResult<SessionEvent> find(SessionEventQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size(),
                Sort.by("occurredAt").descending());
        Instant from = query.from() != null ? query.from() : EARLIEST_EVENT;
        Instant to = query.to() != null ? query.to() : LATEST_EVENT;

        Page<SessionEventDocument> page = findPage(query, from, to, pageable);
        Page<SessionEvent> events = page.map(SessionEventMongoMapper::toDomain);
        return new PageResult<>(
                events.getContent(),
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages());
    }

    /**
     * Selecciona la consulta derivada adecuada. No se usa una única consulta con
     * parámetros nulos porque MongoDB interpretaría {@code campo: null} como
     * "campo nulo o ausente", no como "filtro desactivado".
     */
    private Page<SessionEventDocument> findPage(SessionEventQuery query, Instant from, Instant to,
            PageRequest pageable) {
        boolean hasEmail = query.email() != null;
        boolean hasType = query.type() != null;

        if (hasEmail && hasType) {
            return sessionEventMongoRepository.findByUserEmailAndEventTypeAndOccurredAtBetween(
                    query.email(), query.type(), from, to, pageable);
        }
        if (hasEmail) {
            return sessionEventMongoRepository.findByUserEmailAndOccurredAtBetween(
                    query.email(), from, to, pageable);
        }
        if (hasType) {
            return sessionEventMongoRepository.findByEventTypeAndOccurredAtBetween(
                    query.type(), from, to, pageable);
        }
        return sessionEventMongoRepository.findByOccurredAtBetween(from, to, pageable);
    }
}
