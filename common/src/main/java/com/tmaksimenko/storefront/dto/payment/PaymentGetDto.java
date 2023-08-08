package com.tmaksimenko.storefront.dto.payment;

import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGetDto {

    PaymentStatus paymentStatus;

    PaymentProvider paymentProvider;

}
