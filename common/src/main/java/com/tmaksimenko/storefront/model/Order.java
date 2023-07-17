package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import com.tmaksimenko.storefront.model.payment.Payment;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="orders")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "orderProducts")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;

    @OneToMany( mappedBy = "order",
                cascade = CascadeType.ALL,
                orphanRemoval = true )
    Set<OrderProduct> orderProducts = new HashSet<>();

    @OneToOne(mappedBy = "order")
    Payment payment;

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

    @SuppressWarnings("unused")
    public void clear () {
        orderProducts.clear();
    }

    public OrderDto toPlainDto() {
         return OrderDto.builder()
                .id(this.getId())
                .username(this.account.getUsername())
                .items(this.orderProducts.stream().map(OrderProduct::toDto).collect(Collectors.toList()))
                 .paymentDto(this.getPayment().toDto())
                .build();
    }

    public OrderDto toFullDto() {
        OrderDto orderDto = this.toPlainDto();
        orderDto.setAudit(this.getAudit());
        return orderDto;
    }

}
