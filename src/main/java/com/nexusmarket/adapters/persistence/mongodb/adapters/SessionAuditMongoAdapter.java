package com.nexusmarket.adapters.persistence.mongodb.adapters;

import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.adapters.persistence.mongodb.mappers.SessionEventMongoMapper;
import com.nexusmarket.adapters.persistence.mongodb.repositories.SessionEventMongoRepository;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de persistencia MongoDB para el puerto de auditoría de sesiones.
 * Implementa {@link SessionAuditPort} usando Spring Data MongoDB.
 *
 * <p>Toda operación de persistencia está envuelta en try/catch: si MongoDB falla,
 * se loguea ERROR pero el flujo de negocio continúa (la auditoría no debe bloquear
 * el login/logout/cambio de contraseña).</p>
 */
@Repository
@RequiredArgsConstructor
public class SessionAuditMongoAdapter implements SessionAuditPort {

    private static final Logger log = LoggerFactory.getLogger(SessionAuditMongoAdapter.class);

    private final SessionEventMongoRepository sessionEventMongoRepository;

    @Override
    public void record(SessionAuditPort.SessionEventData event) {
        if (event == null) {
            return;
        }
        try {
            SessionEventDocument document = SessionEventMongoMapper.toDocument(event);
            sessionEventMongoRepository.save(document);
        } catch (Exception ex) {
            // No romper el flujo de negocio si la auditoría falla
            log.error("Fallo al persistir evento de sesión en MongoDB: type={}, email={}",
                    event.type(), event.userEmail(), ex);
        }
    }
}
