package com.tmaksimenko.storefront.dto;

import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.model.payment.Payment;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentCreateDto {

    PaymentInfo paymentInfo;

    PaymentProvider paymentProvider;

    public Payment toPayment () {
        return Payment.builder()
                .paymentStatus(PaymentStatus.NOT_PAID)
                .paymentInfo(paymentInfo)
                .paymentProvider(paymentProvider).build();
    }

}
