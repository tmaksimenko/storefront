package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.OrderProduct.OrderProduct;
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
import java.util.stream.Collectors;

@Entity
@Table(name="orders")
@Data
@EqualsAndHashCode(exclude = "orderProducts")
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;

    @CreationTimestamp
    Instant createTime;

    @UpdateTimestamp
    Instant updateTime;

    @OneToMany( mappedBy = "order",
                cascade = CascadeType.ALL,
                orphanRemoval = true )
    Set<OrderProduct> orderProducts = new HashSet<>();

    public void addProduct (Product product, int quantity) {
        OrderProduct orderProduct = new OrderProduct(this, product,
                (quantity == 0) ? 1 : quantity
                );
        orderProducts.add(orderProduct);
    }

    public void changeProductQuantity (Long productId, int quantity) {
        for (OrderProduct orderProduct : orderProducts) {
            if (orderProduct.getProduct().getId() == productId) {
                if (quantity == 0)
                    orderProducts.remove(orderProduct);
                else
                    orderProduct.setQuantity(quantity);
                break;
            }
        }
    }

    public void clear () {
        orderProducts.clear();
    }

    public OrderDto toPlainDto() {
         return OrderDto.builder()
                .id(this.id)
                .username(this.account.getUsername())
                .items(this.orderProducts.stream().map(OrderProduct::toDto).collect(Collectors.toList()))
                .build();
    }

    public OrderDto toFullDto() {
        OrderDto orderDto = this.toPlainDto();
        orderDto.setCreateTime(this.createTime);
        orderDto.setUpdateTime(this.updateTime);
        return orderDto;
    }

}
