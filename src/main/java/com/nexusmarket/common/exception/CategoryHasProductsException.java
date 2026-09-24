package com.nexusmarket.common.exception;

public class CategoryHasProductsException extends RuntimeException {

    public CategoryHasProductsException(String message) {
        super(message);
    }

    public CategoryHasProductsException(String resource, String field, Object value) {
        super(String.format("Cannot delete %s because it contains products (%s: '%s')", resource, field, value));
    }
}
