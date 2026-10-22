package com.nexusmarket.adapters.rest.controllers.admin;

import com.nexusmarket.adapters.rest.dtos.responses.SessionEventDto;
import com.nexusmarket.adapters.rest.mappers.SessionEventRestMapper;
import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.admin.SessionEventQueryUseCasePort;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controlador de consulta de eventos de sesión de seguridad.
 * Solo accesible por usuarios con rol ADMIN.
 *
 * <p>La consulta se delega a {@link SessionEventQueryUseCasePort}; el
 * controlador solo traduce parámetros HTTP y el resultado paginado.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/session-events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SessionEventController {

    private final SessionEventQueryUseCasePort sessionEventQueryUseCasePort;

    @GetMapping
    public ResponseEntity<Page<SessionEventDto>> getSessionEvents(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) SessionEventType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SessionEventQuery query = new SessionEventQuery(email, type, from, to, page, size);
        PageResult<SessionEvent> result = sessionEventQueryUseCasePort.findSessionEvents(query);

        Page<SessionEventDto> dtoPage = new PageImpl<>(
                result.content().stream()
                        .map(SessionEventRestMapper::toResponseDTO)
                        .toList(),
                PageRequest.of(result.page(), result.size()),
                result.totalElements());

        return ResponseEntity.ok(dtoPage);
    }
}
