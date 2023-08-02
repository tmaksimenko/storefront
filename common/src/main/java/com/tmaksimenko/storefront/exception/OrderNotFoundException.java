package com.tmaksimenko.storefront.exception;

import jakarta.persistence.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException() {}
}
