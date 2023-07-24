package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    List<CartItemDto> cartItemDtos;

    PaymentCreateDto paymentCreateDto;

}

