package com.tmaksimenko.storefront.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class Address {
    String streetAddress;
    String postalCode;
    String country;
}
