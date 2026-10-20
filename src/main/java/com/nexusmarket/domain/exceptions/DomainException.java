package com.nexusmarket.domain.exceptions;

/**
 * Raíz de todas las excepciones de dominio de NexusMarket.
 *
 * <p>El dominio no conoce HTTP ni Spring: esta clase solo expresa que una regla
 * de negocio fue violada. La traducción a códigos de estado HTTP es
 * responsabilidad exclusiva del adaptador REST
 * ({@code GlobalExceptionHandler}).</p>
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
