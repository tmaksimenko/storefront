package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

public class AccountNotFoundException extends EntityNotFoundException {
    public AccountNotFoundException() {}
}
