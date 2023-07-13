package com.tmaksimenko.storefront.dto.product;

import lombok.*;

@Getter
@AllArgsConstructor
public class ProductAdditionDto {
    Long productId;
    int quantity;

    @SuppressWarnings("unused")
    public ProductAdditionDto (Long productId) {
        this.productId = productId;
        this.quantity = 1;
    }
}
