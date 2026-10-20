package com.nexusmarket.domain.exceptions;

public class RefundNotAllowedException extends DomainException {

    public RefundNotAllowedException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Refund not allowed for %s with %s '%s'", resourceName, fieldName, fieldValue));
    }
}
