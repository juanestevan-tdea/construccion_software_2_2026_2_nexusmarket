package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.services.ConsultAuditLogsService;
import com.nexusmarket.domain.services.ConsultSessionEventsService;
import com.nexusmarket.domain.services.RegisterAuditLogService;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.infrastructure.security.AsyncSessionAuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registro de los servicios de auditoría y del decorador asíncrono de sesiones.
 *
 * <p>Los servicios de dominio son POJO puros. El único bean de infraestructura
 * es {@link AsyncSessionAuditService}, que conserva {@code @Async} sin acoplar
 * {@link SessionAuditService} a Spring.</p>
 */
@Configuration
public class AuditServiceConfig {

    @Bean
    public RegisterAuditLogService registerAuditLogService(
            AuditLogRepositoryPort auditLogRepositoryPort) {
        return new RegisterAuditLogService(auditLogRepositoryPort);
    }

    @Bean
    public ConsultAuditLogsService consultAuditLogsService(
            AuditLogRepositoryPort auditLogRepositoryPort) {
        return new ConsultAuditLogsService(auditLogRepositoryPort);
    }

    @Bean
    public ConsultSessionEventsService consultSessionEventsService(
            SessionAuditPort sessionAuditPort) {
        return new ConsultSessionEventsService(sessionAuditPort);
    }

    @Bean
    public SessionAuditService sessionAuditService(SessionAuditPort sessionAuditPort) {
        // El decorador de infraestructura aporta @Async; el dominio sigue siendo POJO.
        return new AsyncSessionAuditService(sessionAuditPort);
    }
}
