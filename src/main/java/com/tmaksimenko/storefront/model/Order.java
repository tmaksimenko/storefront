package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.OrderProduct.OrderProduct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@Table(name="orders")
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

    @OneToMany( mappedBy = "order",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
    Set<OrderProduct> orderProducts;

    public Order (boolean initializeSet) {
        if (initializeSet) this.orderProducts = new HashSet<>();
    }

    public void addProduct (Product product, int quantity) {
        OrderProduct orderProduct = new OrderProduct(this, product,
                (quantity == 0) ? 1 : quantity
                );
        orderProducts.add(orderProduct);
        product.getOrderProducts().add(orderProduct);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return id != null && id.equals(((Order) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public OrderDto toDto() {
        return new OrderDto(this.id, this.account.getUsername(),
                this.orderProducts.stream().map(OrderProduct::toDto).collect(Collectors.toList()));
    }

}
