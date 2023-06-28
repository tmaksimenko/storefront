package com.tmaksimenko.storefront.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@EqualsAndHashCode(exclude = "orders")
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue
    Long id;

    String username;

    String email;

    String password;

    @CreationTimestamp
    Instant createTime;

    @UpdateTimestamp
    Instant lastModified;

    @Embedded
    Address address;

    @OneToMany(mappedBy = "account")
    Set<Order> orders = new HashSet<>();

}
