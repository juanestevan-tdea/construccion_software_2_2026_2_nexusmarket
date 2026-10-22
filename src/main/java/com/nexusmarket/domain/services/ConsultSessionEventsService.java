package com.nexusmarket.domain.services;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Consulta de eventos de sesión.
 *
 * <p>Responsabilidad única: leer la auditoría de sesiones a través de
 * {@link SessionAuditPort}. No registra eventos ni conoce HTTP/MongoDB.</p>
 */
@Service
@RequiredArgsConstructor
public class ConsultSessionEventsService {

    private final SessionAuditPort sessionAuditPort;

    public PageResult<SessionEvent> findSessionEvents(SessionEventQuery query) {
        return sessionAuditPort.find(query);
    }
}
