package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException() {}

    public OrderNotFoundException(Exception cause) {
        super(cause);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
