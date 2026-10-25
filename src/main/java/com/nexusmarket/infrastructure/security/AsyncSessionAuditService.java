package com.nexusmarket.infrastructure.security;

import org.springframework.scheduling.annotation.Async;

import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.services.SessionAuditService;

/**
 * Decorador de infraestructura que conserva el registro asíncrono de eventos de
 * sesión sin acoplar el dominio a Spring.
 *
 * <p>{@link SessionAuditService} es un POJO puro. Esta subclase vive en
 * infraestructura y solo añade {@code @Async} a cada operación pública para que
 * la escritura en MongoDB no bloquee el flujo de autenticación. El bean se
 * declara en {@code AuditServiceConfig} con el tipo base, de modo que los
 * consumidores siguen dependiendo de la clase de dominio.</p>
 */
public class AsyncSessionAuditService extends SessionAuditService {

    public AsyncSessionAuditService(SessionAuditPort sessionAuditPort) {
        super(sessionAuditPort);
    }

    @Override
    @Async
    public void recordLogin(String userEmail, String jti, String ipAddress, String userAgent) {
        super.recordLogin(userEmail, jti, ipAddress, userAgent);
    }

    @Override
    @Async
    public void recordLogout(String userEmail, String jti, String ipAddress, String userAgent) {
        super.recordLogout(userEmail, jti, ipAddress, userAgent);
    }

    @Override
    @Async
    public void recordPasswordChange(String userEmail, String jti, String ipAddress, String userAgent) {
        super.recordPasswordChange(userEmail, jti, ipAddress, userAgent);
    }

    @Override
    @Async
    public void recordTokenRejected(String userEmail, String jti, String reason,
            String ipAddress, String userAgent, Integer tokenVersion) {
        super.recordTokenRejected(userEmail, jti, reason, ipAddress, userAgent, tokenVersion);
    }

    @Override
    @Async
    public void recordTokenInvalid(String jti, String reason, String ipAddress,
            String userAgent, Integer tokenVersion) {
        super.recordTokenInvalid(jti, reason, ipAddress, userAgent, tokenVersion);
    }

    @Override
    @Async
    public void recordLoginFailed(String userEmail, String reason, String ipAddress, String userAgent) {
        super.recordLoginFailed(userEmail, reason, ipAddress, userAgent);
    }
}
