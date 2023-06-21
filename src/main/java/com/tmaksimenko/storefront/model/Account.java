package com.tmaksimenko.storefront.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@Table(name = "accounts")
@EqualsAndHashCode(exclude = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    Long id;

    String username;

    String email;

    String password;

    final Timestamp create_time = new Timestamp(new Date().getTime());

    Timestamp last_modified = null;

    String address;

    String postal_code;

    String country;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    Set<Order> orders;

}
