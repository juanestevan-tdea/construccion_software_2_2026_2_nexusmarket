package com.nexusmarket.domain.exceptions;

public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}
