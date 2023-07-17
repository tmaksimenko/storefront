package com.tmaksimenko.storefront.model.orderProduct;

import com.tmaksimenko.storefront.dto.OrderProductDto;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "orders_products")
@Data
@NoArgsConstructor
public class OrderProduct {
    @EmbeddedId
    OrderProductId id;

    @ManyToOne
    @MapsId("orderId")
    Order order;

    @ManyToOne
    @MapsId("productId")
    Product product;

    int quantity;

    @SuppressWarnings("unused")
    public OrderProduct (Order order, Product product) {
        this.order = order;
        this.product = product;
        this.id = new OrderProductId(order.getId(), product.getId());
    }

    public OrderProduct (Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.id = new OrderProductId(order.getId(), product.getId());
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        OrderProduct that = (OrderProduct) o;
        return Objects.equals(order, that.order) &&
                Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, product);
    }

    public OrderProductDto toDto () {
        return OrderProductDto.builder()
                .productDto(this.product.toDto())
                .quantity(this.quantity)
                .build();
    }

}
