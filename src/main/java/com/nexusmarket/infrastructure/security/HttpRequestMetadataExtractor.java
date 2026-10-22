package com.nexusmarket.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extrae metadatos del cliente desde una petición HTTP.
 *
 * <p>Vive en infraestructura porque depende del servlet container. El servicio
 * de auditoría recibe únicamente cadenas y no conoce HTTP.</p>
 */
public final class HttpRequestMetadataExtractor {

    private static final String UNKNOWN = "unknown";

    private HttpRequestMetadataExtractor() {
    }

    /**
     * Extrae la IP del cliente considerando proxies ({@code X-Forwarded-For}).
     */
    public static String extractIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String remoteAddress = request.getRemoteAddr();
        return remoteAddress != null ? remoteAddress : UNKNOWN;
    }

    /**
     * Extrae el {@code User-Agent} de la petición.
     */
    public static String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : UNKNOWN;
    }
}
