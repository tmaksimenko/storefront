package com.tmaksimenko.storefront.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderProductDto {

    Long orderId;

    ProductDto productDto;

    int quantity;

}
