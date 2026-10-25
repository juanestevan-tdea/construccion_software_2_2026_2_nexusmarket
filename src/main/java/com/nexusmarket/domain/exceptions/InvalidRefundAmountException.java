package com.nexusmarket.domain.exceptions;

/**
 * El monto solicitado para un reembolso no es positivo (HTTP 422).
 */
public class InvalidRefundAmountException extends DomainException {

    public InvalidRefundAmountException(String message) {
        super(message);
    }

    public InvalidRefundAmountException(Object amount) {
        super(String.format("Refund amount must be greater than zero. Provided: %s", amount));
    }
}
