package com.tmaksimenko.storefront.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.dto.PaymentDto;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.model.BaseEntity;
import com.tmaksimenko.storefront.model.Order;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    Order order;

    public PaymentDto toDto () {
        return PaymentDto.builder()
                .id(this.getId())
                .paymentStatus(this.paymentStatus)
                .paymentProvider(this.paymentProvider)
                .build();
    }
}
