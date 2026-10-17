package com.nexusmarket.domain.services;

import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;

/**
 * Servicio de aplicación para registrar eventos de sesión de seguridad.
 * Opera de forma asíncrona (@Async) para no bloquear el flujo de autenticación.
 * Toda la persistencia se delega al puerto {@link SessionAuditPort};
 * si la persistencia falla, se loguea ERROR y el flujo continúa.
 */
@Service
@RequiredArgsConstructor
public class SessionAuditService {

    private static final Logger log = LoggerFactory.getLogger(SessionAuditService.class);

    private final SessionAuditPort sessionAuditPort;

    /**
     * Extrae la IP del cliente considerando proxies (X-Forwarded-For) con fallback a getRemoteAddr.
     */
    private static String extractIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For puede contener una cadena: cliente, proxy1, proxy2
            return xff.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    /**
     * Extrae el User-Agent de la petición.
     */
    private static String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "unknown";
    }

    /**
     * Registra un login exitoso.
     */
    @Async
    public void recordLogin(String userEmail, String jti, HttpServletRequest request) {
        record(userEmail, SessionEventType.LOGIN, jti, null,
                extractIp(request), extractUserAgent(request), null);
    }

    /**
     * Registra un logout.
     */
    @Async
    public void recordLogout(String userEmail, String jti, HttpServletRequest request) {
        record(userEmail, SessionEventType.LOGOUT, jti, null,
                extractIp(request), extractUserAgent(request), null);
    }

    /**
     * Registra un cambio de contraseña.
     */
    @Async
    public void recordPasswordChange(String userEmail, String jti, HttpServletRequest request) {
        record(userEmail, SessionEventType.PASSWORD_CHANGED, jti, null,
                extractIp(request), extractUserAgent(request), null);
    }

    /**
     * Registra un token rechazado por versión (ver) desactualizada.
     */
    @Async
    public void recordTokenRejected(String userEmail, String jti, String reason,
                                    HttpServletRequest request, Integer tokenVersion) {
        record(userEmail, SessionEventType.TOKEN_REJECTED, jti, reason,
                extractIp(request), extractUserAgent(request), tokenVersion);
    }

    /**
     * Registra un token inválido (firma o expiración).
     */
    @Async
    public void recordTokenInvalid(String jti, String reason, HttpServletRequest request,
                                   Integer tokenVersion) {
        record(null, SessionEventType.TOKEN_INVALID, jti, reason,
                extractIp(request), extractUserAgent(request), tokenVersion);
    }

    /**
     * Registra un fallo de login (credenciales inválidas).
     */
    @Async
    public void recordLoginFailed(String userEmail, String reason, HttpServletRequest request) {
        record(userEmail, SessionEventType.LOGIN_FAILED, null, reason,
                extractIp(request), extractUserAgent(request), null);
    }

    private void record(String userEmail, SessionEventType type, String tokenId, String reason,
                        String ipAddress, String userAgent, Integer tokenVersion) {
        SessionAuditPort.SessionEventData event = new SessionAuditPort.SessionEventData(
                userEmail, type, tokenId, reason, ipAddress, userAgent,
                tokenVersion, Instant.now());
        try {
            sessionAuditPort.record(event);
        } catch (Exception ex) {
            // No romper el flujo de negocio si la auditoría falla
            log.error("Fallo al registrar evento de sesión en MongoDB: type={}, email={}, jti={}",
                    type, userEmail, tokenId, ex);
        }
    }
}
