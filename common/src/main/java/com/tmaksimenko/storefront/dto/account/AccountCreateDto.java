package com.tmaksimenko.storefront.dto.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.base.Audit;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountCreateDto extends AccountDto {

    Role role;

    public AccountFullDto toFullDto (String createdBy) {
        return AccountFullDto.builder()
                .username(this.getUsername())
                .email(this.getEmail())
                .password(this.getPassword())
                .role(role)
                .audit(Audit.builder().createdOn(LocalDateTime.now()).createdBy(createdBy).build())
                .build();
    }

}
