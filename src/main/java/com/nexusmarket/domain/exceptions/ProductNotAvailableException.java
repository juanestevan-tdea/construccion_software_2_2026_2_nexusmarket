package com.nexusmarket.domain.exceptions;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(String message) {
        super(message);
    }
}
