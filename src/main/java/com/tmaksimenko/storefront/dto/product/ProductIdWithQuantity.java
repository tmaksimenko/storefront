package com.tmaksimenko.storefront.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductIdWithQuantity {
    Long productId;
    int quantity;

    public ProductIdWithQuantity(Long productId) {
        this.productId = productId;
        this.quantity = 1;
    }
}
