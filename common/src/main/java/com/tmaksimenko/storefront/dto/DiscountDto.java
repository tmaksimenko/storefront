package com.tmaksimenko.storefront.dto;

import com.tmaksimenko.storefront.dto.product.ProductDto;
import lombok.Builder;

import java.util.EnumSet;
import java.util.Set;

@Builder(toBuilder = true)
public class DiscountDto {
    Long id;
    double percent;
    EnumSet roles;
    Set<ProductDto> products;

}
