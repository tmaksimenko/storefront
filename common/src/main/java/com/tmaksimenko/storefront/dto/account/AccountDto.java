package com.tmaksimenko.storefront.dto.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.model.Address;
import com.tmaksimenko.storefront.model.Audit;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDto extends AccountCreateDto {

    Audit audit;

    Address address;

    List<OrderGetDto> orderGetDtos;

}
