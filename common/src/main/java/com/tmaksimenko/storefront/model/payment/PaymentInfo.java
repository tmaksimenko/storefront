package com.tmaksimenko.storefront.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentInfo {

    Long cardNumber;

    @Embedded
    ExpiryDate expiry;

    Integer securityCode;

    @SuppressWarnings("JpaDataSourceORMInspection") // refuses to see column card_postal_code
    @Column(name = "card_postal_code")
    String postalCode;
}
