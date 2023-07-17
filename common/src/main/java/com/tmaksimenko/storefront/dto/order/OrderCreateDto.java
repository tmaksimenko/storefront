package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderCreateDto {

    String username;

    List<ProductCreateDto> productCreateDtos;

}

