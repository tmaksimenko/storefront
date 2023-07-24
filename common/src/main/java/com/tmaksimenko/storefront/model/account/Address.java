package com.tmaksimenko.storefront.model.account;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address {
    String streetAddress;
    String postalCode;
    String country;
}
