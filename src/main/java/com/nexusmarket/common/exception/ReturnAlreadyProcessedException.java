package com.nexusmarket.common.exception;

public class ReturnAlreadyProcessedException extends RuntimeException {

    public ReturnAlreadyProcessedException(String message) {
        super(message);
    }

    public ReturnAlreadyProcessedException(String resource, String field, Object value) {
        super(String.format("Return request already processed for %s (%s: '%s')", resource, field, value));
    }
}
