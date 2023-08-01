package com.tmaksimenko.storefront.dto.account;

import com.tmaksimenko.storefront.model.account.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountUpdateDto {

    AccountDto accountDto;

    Address address;

    public Boolean isNull () {
        if (accountDto == null || address == null)
            return true;
        return (accountDto.getUsername() == null &&
                accountDto.getEmail() == null &&
                accountDto.getPassword() == null &&
                address.getStreetAddress() == null &&
                address.getPostalCode() == null &&
                address.getCountry() == null);
    }

}
