package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@SuperBuilder
@NoArgsConstructor
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

}
