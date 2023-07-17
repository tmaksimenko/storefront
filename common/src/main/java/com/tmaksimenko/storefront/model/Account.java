package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
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

    @Enumerated(value = EnumType.STRING)
    Role role = Role.ROLE_USER;

    @Embedded
    Address address;

    @OneToMany(mappedBy = "account")
    Set<Order> orders = new HashSet<>();

}
