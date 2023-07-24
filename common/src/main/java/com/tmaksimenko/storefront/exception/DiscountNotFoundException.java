package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

@SuppressWarnings("unused")
public class DiscountNotFoundException extends EntityNotFoundException {
    public DiscountNotFoundException() {}

    public DiscountNotFoundException(Exception cause) {
        super(cause);
    }

    public DiscountNotFoundException(String message) {
        super(message);
    }

    public DiscountNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
