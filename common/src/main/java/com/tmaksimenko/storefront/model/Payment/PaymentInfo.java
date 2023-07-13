package com.tmaksimenko.storefront.model.Payment;

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
