package com.tmaksimenko.storefront.dto.account;

import com.tmaksimenko.storefront.model.account.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountUpdateDto {

    AccountDto accountDto;

    Address address;

}
