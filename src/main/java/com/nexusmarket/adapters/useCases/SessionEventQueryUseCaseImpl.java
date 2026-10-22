package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.admin.SessionEventQueryUseCasePort;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.services.ConsultSessionEventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso de solo lectura para eventos de sesión.
 *
 * <p>Normaliza la paginación y delega la consulta a
 * {@code ConsultSessionEventsService}; el controlador no conoce repositorios ni
 * documentos MongoDB.</p>
 */
@Service
@RequiredArgsConstructor
public class SessionEventQueryUseCaseImpl implements SessionEventQueryUseCasePort {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ConsultSessionEventsService consultSessionEventsService;

    @Override
    public PageResult<SessionEvent> findSessionEvents(SessionEventQuery query) {
        int page = Math.max(query.page(), 0);
        int size = query.size() <= 0 ? DEFAULT_PAGE_SIZE : Math.min(query.size(), MAX_PAGE_SIZE);
        SessionEventQuery normalized = new SessionEventQuery(
                query.email(), query.type(), query.from(), query.to(), page, size);
        return consultSessionEventsService.findSessionEvents(normalized);
    }
}
