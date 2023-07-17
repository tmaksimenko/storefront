package com.tmaksimenko.storefront.dto.account;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Audit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
public class AccountCreateDto {

    String username;

    String email;

    String password;

    public AccountDto toFullDto (Role role, Audit audit) {
        return AccountDto.builder().username(username).email(email).password(password).role(role).audit(audit).build();
    }

}
