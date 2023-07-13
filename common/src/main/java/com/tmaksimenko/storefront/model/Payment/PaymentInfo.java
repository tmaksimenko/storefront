package com.tmaksimenko.storefront.model.Payment;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class PaymentInfo {
    Long cardNumber;
    ExpiryDate expiry;
    int securityCode;
    String postalCode;
}
