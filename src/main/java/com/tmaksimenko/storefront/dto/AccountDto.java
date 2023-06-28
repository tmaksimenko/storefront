package com.tmaksimenko.storefront.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.Address;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {

    String username;

    String email;

    String password;

    Address address;

    List<OrderDto> orders = new ArrayList<>();

}
