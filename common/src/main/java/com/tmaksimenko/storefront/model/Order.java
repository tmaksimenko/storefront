package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.base.BaseEntity;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import com.tmaksimenko.storefront.model.payment.Payment;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="orders")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = "orderProducts")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;

    @OneToMany( mappedBy = "order",
                cascade = CascadeType.ALL,
                orphanRemoval = true )
    @Builder.Default
    Set<OrderProduct> orderProducts = new HashSet<>();

    @Embedded
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

    public OrderGetDto toPlainDto() {
         return OrderGetDto.builder()
                .id(this.getId())
                .username(this.account.getUsername())
                .items(this.orderProducts.stream().map(OrderProduct::toDto).collect(Collectors.toList()))
                 .paymentGetDto(this.getPayment().toDto())
                .build();
    }

    public OrderGetDto toFullDto() {
        OrderGetDto orderGetDto = this.toPlainDto();
        orderGetDto.setAudit(this.getAudit());
        return orderGetDto;
    }

}
