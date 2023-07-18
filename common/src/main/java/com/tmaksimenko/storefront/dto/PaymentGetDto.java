package com.tmaksimenko.storefront.dto;

import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentGetDto {

    PaymentStatus paymentStatus;

    PaymentProvider paymentProvider;

}
