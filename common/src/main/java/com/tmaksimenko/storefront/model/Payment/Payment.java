package com.tmaksimenko.storefront.model.Payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.enums.PaymentProvider;
import com.tmaksimenko.storefront.enums.PaymentStatus;
import com.tmaksimenko.storefront.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment extends BaseEntity {
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    PaymentProvider paymentProvider;

    @JsonIgnore
    @Embedded
    PaymentInfo paymentInfo;
}
