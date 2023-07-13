package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

@SuppressWarnings("unused")
public class AccountNotFoundException extends EntityNotFoundException {
    public AccountNotFoundException() {}

    public AccountNotFoundException(Exception cause) {
        super(cause);
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException (String message, Exception cause) {
        super(message, cause);
    }
}
