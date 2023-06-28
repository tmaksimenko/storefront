package com.tmaksimenko.storefront.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.product.ProductDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
