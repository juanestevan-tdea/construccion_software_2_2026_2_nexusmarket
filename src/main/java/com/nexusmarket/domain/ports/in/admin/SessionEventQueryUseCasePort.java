package com.nexusmarket.domain.ports.in.admin;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;

/**
 * Puerto de entrada para consultar los eventos de sesión de seguridad.
 */
public interface SessionEventQueryUseCasePort {

    PageResult<SessionEvent> findSessionEvents(SessionEventQuery query);
}
