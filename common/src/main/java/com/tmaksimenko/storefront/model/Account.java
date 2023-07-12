package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.enums.Role;
import jakarta.persistence.*;
import lombok.*;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue
    Long id;

    String username;

    String email;

    String password;

    @Enumerated(value = EnumType.STRING)
    Role role = Role.ROLE_USER;

    @Embedded
    Address address;

    @CreationTimestamp
    Instant createTime;

    @UpdateTimestamp
    Instant lastModified;

    @OneToMany(mappedBy = "account")
    Set<Order> orders = new HashSet<>();

}
