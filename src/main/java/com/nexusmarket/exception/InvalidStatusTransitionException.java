package com.nexusmarket.exception;

/**
 * Thrown when an entity is moved to an invalid lifecycle state, for example
 * dispatching an order that has not been paid. Maps to HTTP 409 (Conflict).
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition: cannot move from '%s' to '%s'", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }
}
