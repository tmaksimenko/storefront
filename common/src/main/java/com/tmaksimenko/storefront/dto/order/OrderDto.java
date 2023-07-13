package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.OrderProductDto;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.model.Payment.Payment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderDto {

    Long id;

    String username;

    Audit audit;

    Payment payment;

    List<OrderProductDto> items;
}
