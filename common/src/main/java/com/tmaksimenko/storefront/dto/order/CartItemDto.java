package com.tmaksimenko.storefront.dto.order;

import lombok.*;

@Data
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
