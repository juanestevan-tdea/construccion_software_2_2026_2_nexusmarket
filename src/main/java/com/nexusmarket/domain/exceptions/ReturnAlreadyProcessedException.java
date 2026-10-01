package com.nexusmarket.domain.exceptions;

public class ReturnAlreadyProcessedException extends RuntimeException {

    public ReturnAlreadyProcessedException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Return request for %s with %s '%s' has already been processed or is pending",
                resourceName, fieldName, fieldValue));
    }
}
