package com.tmaksimenko.storefront.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.dto.payment.PaymentGetDto;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Embeddable
@SuperBuilder
@NoArgsConstructor
@Data
public class Payment {
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    PaymentProvider paymentProvider;

    @JsonIgnore
    @Embedded
    PaymentInfo paymentInfo;

    public PaymentGetDto toDto () {
        return PaymentGetDto.builder()
                .paymentStatus(this.paymentStatus)
                .paymentProvider(this.paymentProvider)
                .build();
    }
}
