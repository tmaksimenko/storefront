package com.tmaksimenko.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductDto {

    long id;

    String name;

    String brand;

    Double price;

}