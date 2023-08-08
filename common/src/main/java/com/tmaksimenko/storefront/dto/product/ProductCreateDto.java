package com.tmaksimenko.storefront.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ProductCreateDto {

    String name;

    String brand;

    Double price;

    Double weight;

}
