package com.tmaksimenko.storefront.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;

@Embeddable
@Data
public class PaymentInfo {

    Long cardNumber;

    @Embedded
    ExpiryDate expiry;

    int securityCode;

    @SuppressWarnings("all") // refuses to see column card_postal_code
    @Column(name = "card_postal_code")
    String postalCode;
}
