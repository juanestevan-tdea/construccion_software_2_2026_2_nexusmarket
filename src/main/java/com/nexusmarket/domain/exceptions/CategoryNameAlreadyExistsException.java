package com.nexusmarket.domain.exceptions;

/**
 * Ya existe una categoría con el nombre indicado (HTTP 409).
 */
public class CategoryNameAlreadyExistsException extends DuplicateResourceException {

    public CategoryNameAlreadyExistsException(String name) {
        super("Category", "name", name);
    }
}
