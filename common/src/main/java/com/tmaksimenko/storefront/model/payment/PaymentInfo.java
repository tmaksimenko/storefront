package com.tmaksimenko.storefront.model.payment;

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
    String postalCode;
}
