package com.tmaksimenko.storefront.dto.product;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ProductDto {

    Long id;

    String name;

    String brand;

    Double price;

}
