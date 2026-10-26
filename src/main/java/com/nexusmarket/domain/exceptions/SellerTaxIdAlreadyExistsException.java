package com.nexusmarket.domain.exceptions;

/**
 * Ya existe un vendedor con el identificador tributario indicado (HTTP 409).
 */
public class SellerTaxIdAlreadyExistsException extends DuplicateResourceException {

    public SellerTaxIdAlreadyExistsException(String taxId) {
        super("Seller", "taxId", taxId);
    }
}
