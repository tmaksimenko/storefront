package com.tmaksimenko.storefront.model.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(exclude = {"orders", "cart"})
@EqualsAndHashCode(exclude = {"orders", "cart"})
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

    @Transient
    public AccountFullDto toDto () {
        List<OrderGetDto> orderGetDtos = null;
        if (this.getOrders() != null)
            orderGetDtos = this.getOrders().stream().map(Order::toPlainDto).toList();

        return AccountFullDto.builder()
                .username(this.getUsername())
                .email(this.getEmail())
                .password(this.getPassword())
                .address(this.getAddress())
                .role(this.getRole())
                .audit(this.getAudit())
                .cart(this.getCart())
                .orderGetDtos(orderGetDtos)
                .build();
    }

}
