package com.tmaksimenko.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductCreateDto {

    String name;

    String brand;

    Double price;

    Double weight;

}
