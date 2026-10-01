package com.nexusmarket.domain.exceptions;

public class InvalidStatusTransitionException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Cannot transition from status '%s' to '%s'", currentStatus, targetStatus));
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

