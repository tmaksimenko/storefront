package com.tmaksimenko.storefront.dto.order;

import com.tmaksimenko.storefront.dto.PaymentCreateDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDto {

    List<ProductCreateDto> productCreateDtos;

    PaymentCreateDto paymentCreateDto;

    public OrderDto toOrderDto (String username) {
        return OrderDto.builder()
                .username(username)
                .productCreateDtos(productCreateDtos)
                .paymentCreateDto(paymentCreateDto).build();
    }

}

