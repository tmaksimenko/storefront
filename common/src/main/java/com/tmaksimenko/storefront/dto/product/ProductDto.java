package com.tmaksimenko.storefront.dto.product;

import com.tmaksimenko.storefront.dto.DiscountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductDto {

    long id;

    String name;

    String brand;

    float price;

    List<DiscountDto> discounts;

}
