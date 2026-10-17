package com.nexusmarket.domain.valueobjects;

/**
 * Tipos de evento de sesión registrados para trazabilidad de seguridad en MongoDB.
 */
public enum SessionEventType {
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGED,
    TOKEN_REJECTED,
    TOKEN_INVALID,
    LOGIN_FAILED
}
