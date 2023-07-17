package com.tmaksimenko.storefront.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.Address;
import com.tmaksimenko.storefront.model.Audit;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDto {

    String username;

    String email;

    @JsonIgnore
    String password;

    Audit audit;

    Address address;

    List<OrderDto> orders;

}
