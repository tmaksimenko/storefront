package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.OrderProductDto;
import com.tmaksimenko.storefront.dto.payment.PaymentGetDto;
import com.tmaksimenko.storefront.model.base.Audit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderGetDto {

    Long id;

    String username;

    Audit audit;

    PaymentGetDto paymentGetDto;

    List<OrderProductDto> items;
}
