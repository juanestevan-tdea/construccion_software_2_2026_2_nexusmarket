package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;

/**
 * Puerto de salida para registrar y consultar eventos de sesión de auditoría.
 * La implementación concreta (MongoDB) vive en el adaptador;
 * el dominio solo conoce este contrato.
 */
public interface SessionAuditPort {

    /**
     * Registra un evento de sesión.
     * Los fallos de persistencia se propagan al llamador. El servicio de
     * aplicación que consume este puerto decide si la auditoría puede tolerarse
     * sin romper el flujo de negocio; el adaptador no oculta el error.
     *
     * @param event datos del evento
     */
    void record(SessionEvent event);

    /**
     * Consulta paginada de eventos de sesión.
     *
     * @param query filtros y paginación
     * @return página de eventos
     */
    PageResult<SessionEvent> find(SessionEventQuery query);
}
