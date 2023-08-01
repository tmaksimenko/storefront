package com.tmaksimenko.storefront.dto.account;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.base.Audit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    String username;

    String email;

    String password;

    public AccountFullDto toFullDto (Role role, String createdBy) {
        return AccountFullDto.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .audit(Audit.builder().createdOn(LocalDateTime.now()).createdBy(createdBy).build())
                .build();
    }
    public AccountFullDto toFullDto (Role role) {
        return AccountFullDto.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

}
