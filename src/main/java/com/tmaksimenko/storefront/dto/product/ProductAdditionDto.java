package com.tmaksimenko.storefront.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAdditionDto {
    Long productId;
    int quantity;

    public ProductAdditionDto(Long productId) {
        this.productId = productId;
        this.quantity = 1;
    }
}
