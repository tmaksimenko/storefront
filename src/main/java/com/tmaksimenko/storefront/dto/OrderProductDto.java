package com.tmaksimenko.storefront.dto;

import com.tmaksimenko.storefront.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductDto {

    Long orderId;

    ProductDto productDto;

    int quantity;

    public OrderProductDto (Long orderId, ProductDto productDto) {
        this.orderId = orderId;
        this.productDto = productDto;
        this.quantity = 1;
    }

}
