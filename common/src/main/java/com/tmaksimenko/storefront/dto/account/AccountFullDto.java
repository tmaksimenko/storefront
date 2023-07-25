package com.tmaksimenko.storefront.dto.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.base.Audit;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountFullDto extends AccountDto {

    Audit audit;

    Role role;

    Address address;

    Cart cart;

    List<OrderGetDto> orderGetDtos;

    public Account toNewAccount () {
        return Account.builder()
                .username(this.getUsername())
                .email(this.getEmail())
                .password(this.getPassword())
                .audit(this.audit)
                .role(this.role)
                .address(this.address)
                .build();
    }

}
