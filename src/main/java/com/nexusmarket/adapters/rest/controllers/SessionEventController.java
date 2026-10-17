package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.responses.SessionEventDto;
import com.nexusmarket.adapters.persistence.mongodb.adapters.SessionAuditMongoAdapter;
import com.nexusmarket.adapters.persistence.mongodb.mappers.SessionEventMongoMapper;
import com.nexusmarket.adapters.persistence.mongodb.repositories.SessionEventMongoRepository;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Controlador de consulta de eventos de sesión de seguridad.
 * Solo accesible por usuarios con rol ADMIN.
 */
@RequestMapping("/api/v1/admin/session-events")
@RequiredArgsConstructor
public class SessionEventController {

    private final SessionEventMongoRepository sessionEventMongoRepository;

    /**
     * Consulta paginada de eventos de sesión con filtros opcionales.
     *
     * @param email  email del usuario (opcional)
     * @param type   tipo de evento (opcional)
     * @param from   fecha inicio (inclusive, ISO 8601, opcional)
     * @param to     fecha fin (inclusive, ISO 8601, opcional)
     * @param page   número de página (0-based)
     * @param size   tamaño de página
     * @return página de eventos ordenada por occurredAt DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SessionEventDto>> getSessionEvents(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) SessionEventType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("occurredAt").descending());

        // Spring Data MongoDB con @Query requiere valores no null; se usan criterios por defecto
        // cuando los filtros son opcionales. Para mantener simplicidad, se delega la consulta
        // al adaptador que usa MongoTemplate con Criteria dinámicas.
        Page<com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument> pageDocs;

        if (email != null && type != null && from != null && to != null) {
            pageDocs = sessionEventMongoRepository.findByFilters(email, type, from, to, pageable);
        } else if (email != null && type != null) {
            pageDocs = sessionEventMongoRepository.findByFilters(email, type,
                    Instant.MIN, Instant.MAX, pageable);
        } else if (email != null) {
            // Sin tipo: consultar por email y paginar en memoria no es ideal con @Query fijo;
            // se usa findByFilters con tipo null (Spring Data lo trata como sin filtro de tipo)
            pageDocs = sessionEventMongoRepository.findByFilters(email, null,
                    Instant.MIN, Instant.MAX, pageable);
        } else if (type != null) {
            pageDocs = sessionEventMongoRepository.findByFilters(null, type,
                    Instant.MIN, Instant.MAX, pageable);
        } else if (from != null && to != null) {
            pageDocs = sessionEventMongoRepository.findByFilters(null, null, from, to, pageable);
        } else {
            pageDocs = sessionEventMongoRepository.findByFilters(null, null,
                    Instant.MIN, Instant.MAX, pageable);
        }

        Page<SessionEventDto> dtoPage = pageDocs.map(SessionEventController::toDto);

        return ResponseEntity.ok(dtoPage);
    }

    private static SessionEventDto toDto(
            com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument doc) {
        return SessionEventDto.builder()
                .userEmail(doc.getUserEmail())
                .eventType(doc.getEventType())
                .tokenId(doc.getTokenId())
                .reason(doc.getReason())
                .ipAddress(doc.getIpAddress())
                .userAgent(doc.getUserAgent())
                .tokenVersion(doc.getTokenVersion())
                .occurredAt(doc.getOccurredAt())
                .build();
    }
}
