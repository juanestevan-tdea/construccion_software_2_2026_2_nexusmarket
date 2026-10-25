package com.nexusmarket.domain.services;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Servicio de aplicación para registrar eventos de sesión de seguridad.
 *
 * <p>Este servicio es un POJO puro y no conoce Spring. El carácter asíncrono lo
 * aporta el decorador {@code AsyncSessionAuditService} en infraestructura, para
 * no bloquear el flujo de autenticación. Toda la persistencia se delega al
 * puerto {@link SessionAuditPort}; si el registro falla, se loguea ERROR y el
 * flujo continúa.</p>
 *
 * <p>Este servicio no conoce {@code HttpServletRequest}: la extracción de IP y
 * User-Agent corresponde al adaptador web.</p>
 */
@RequiredArgsConstructor
public class SessionAuditService {

    private static final Logger log = LoggerFactory.getLogger(SessionAuditService.class);
    private static final String UNKNOWN = "unknown";

    private final SessionAuditPort sessionAuditPort;

    /**
     * Registra un login exitoso.
     */
    public void recordLogin(String userEmail, String jti, String ipAddress, String userAgent) {
        record(userEmail, SessionEventType.LOGIN, jti, null, ipAddress, userAgent, null);
    }

    /**
     * Registra un logout.
     */
    public void recordLogout(String userEmail, String jti, String ipAddress, String userAgent) {
        record(userEmail, SessionEventType.LOGOUT, jti, null, ipAddress, userAgent, null);
    }

    /**
     * Registra un cambio de contraseña.
     */
    public void recordPasswordChange(String userEmail, String jti, String ipAddress, String userAgent) {
        record(userEmail, SessionEventType.PASSWORD_CHANGED, jti, null, ipAddress, userAgent, null);
    }

    /**
     * Registra un token rechazado por versión (ver) desactualizada.
     */
    public void recordTokenRejected(String userEmail, String jti, String reason,
            String ipAddress, String userAgent, Integer tokenVersion) {
        record(userEmail, SessionEventType.TOKEN_REJECTED, jti, reason,
                ipAddress, userAgent, tokenVersion);
    }

    /**
     * Registra un token inválido (firma o expiración).
     */
    public void recordTokenInvalid(String jti, String reason, String ipAddress,
            String userAgent, Integer tokenVersion) {
        record(null, SessionEventType.TOKEN_INVALID, jti, reason,
                ipAddress, userAgent, tokenVersion);
    }

    /**
     * Registra un fallo de login (credenciales inválidas).
     */
    public void recordLoginFailed(String userEmail, String reason, String ipAddress, String userAgent) {
        record(userEmail, SessionEventType.LOGIN_FAILED, null, reason,
                ipAddress, userAgent, null);
    }

    private void record(String userEmail, SessionEventType type, String tokenId, String reason,
            String ipAddress, String userAgent, Integer tokenVersion) {
        SessionEvent event = new SessionEvent(
                userEmail, type, tokenId, reason, normalize(ipAddress), normalize(userAgent),
                tokenVersion, Instant.now());
        try {
            sessionAuditPort.record(event);
        } catch (Exception ex) {
            // No romper el flujo de negocio si la auditoría falla
            log.error("Fallo al registrar evento de sesión en MongoDB: type={}, email={}, jti={}",
                    type, userEmail, tokenId, ex);
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}
