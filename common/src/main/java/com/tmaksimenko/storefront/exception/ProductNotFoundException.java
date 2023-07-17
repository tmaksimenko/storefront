package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

@SuppressWarnings("unused")
public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException() {}

    public ProductNotFoundException(Exception cause) {
        super(cause);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
