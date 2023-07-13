package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.product.ProductAdditionDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderCreateDto {

    String username;

    List<ProductAdditionDto> productAdditionDtos;

}

