package com.tmaksimenko.storefront.dto.product;

import lombok.*;

@Getter
@AllArgsConstructor
public class ProductCreateDto {
    Long productId;
    int quantity;

    @SuppressWarnings("unused")
    public ProductCreateDto(Long productId) {
        this.productId = productId;
        this.quantity = 1;
    }
}
