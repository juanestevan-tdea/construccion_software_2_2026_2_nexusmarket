package com.nexusmarket.domain.exceptions;

public class CategoryHasProductsException extends RuntimeException {

    public CategoryHasProductsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Cannot delete %s with %s '%s' because it has associated products",
                resourceName, fieldName, fieldValue));
    }
}
