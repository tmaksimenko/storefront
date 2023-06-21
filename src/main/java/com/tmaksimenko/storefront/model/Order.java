package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.OrderDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name="orders")
@EqualsAndHashCode(exclude = {"account", "products"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @Column
    Timestamp create_time = new Timestamp(new Date().getTime());

    @ManyToMany
    @JoinTable(
            name = "orders_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    Set<Product> products;

    public OrderDto toDto() {
        return new OrderDto(this.id, this.account.getUsername(),
                this.products.stream().map(Product::toDto).collect(Collectors.toList()));
    }

}
