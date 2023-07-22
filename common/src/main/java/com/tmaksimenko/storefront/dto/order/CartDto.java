package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.PaymentCreateDto;
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

    /*public CartDto merge (CartDto cartDto) {
        List<CartItemDto> mergedCartItemDtos = this.cartItemDtos;
        mergedCartItemDtos.addAll(cartDto.getCartItemDtos());
        return new CartDto(mergedCartItemDtos);
    }*/

}

