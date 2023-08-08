package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

public class DiscountNotFoundException extends EntityNotFoundException {
    public DiscountNotFoundException() {}
}
