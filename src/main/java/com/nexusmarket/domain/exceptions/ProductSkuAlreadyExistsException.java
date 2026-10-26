package com.nexusmarket.domain.exceptions;

/**
 * Ya existe un producto con el SKU indicado (HTTP 409).
 */
public class ProductSkuAlreadyExistsException extends DuplicateResourceException {

    public ProductSkuAlreadyExistsException(String sku) {
        super("Product", "sku", sku);
    }
}
