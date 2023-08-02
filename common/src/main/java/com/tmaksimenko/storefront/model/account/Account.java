package com.tmaksimenko.storefront.model.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(exclude = "orders")
@EqualsAndHashCode(exclude = "orders")
public class Account extends BaseEntity {

    String username;

    String email;

    String password;

    @Enumerated(EnumType.STRING)
    Role role;

    @Embedded
    Address address;

    @Embedded
    Cart cart;

    @OneToMany(mappedBy = "account")
    Set<Order> orders;

    public AccountFullDto toDto () {
        return AccountFullDto.builder()
                .username(this.getUsername())
                .email(this.getEmail())
                .password(this.getPassword())
                .address(this.getAddress())
                .role(this.getRole())
                .audit(this.getAudit())
                .build();
    }

}
