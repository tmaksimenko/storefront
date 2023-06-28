package com.tmaksimenko.storefront.model.OrderProduct;

import com.tmaksimenko.storefront.dto.OrderProductDto;
import com.tmaksimenko.storefront.model.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.tmaksimenko.storefront.model.Order;

import java.util.Objects;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders_products")
public class OrderProduct {
    @EmbeddedId
    OrderProductId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    Product product;

    int quantity;

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
        return (new OrderProductDto(this.order.getId(), this.product.toDto(), this.quantity));
    }

}
