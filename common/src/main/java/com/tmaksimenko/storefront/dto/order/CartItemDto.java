package com.tmaksimenko.storefront.dto.order;

import lombok.*;

@Getter
@AllArgsConstructor
public class CartItemDto {

    Long productId;
    int quantity;

    @SuppressWarnings("unused")
    public CartItemDto(Long productId) {
        this.productId = productId;
        this.quantity = 1;
    }
}
