package com.tmaksimenko.storefront.dto;

import com.tmaksimenko.storefront.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountCreateDto {

    String username;

    String email;

    String password;

    Role role;

}
