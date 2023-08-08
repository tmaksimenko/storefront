package com.tmaksimenko.storefront.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="products")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "orderProducts")
@ToString(exclude = "orderProducts")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq")
    long id;

    String name;

    String brand;

    Double price;

    Double weight;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<OrderProduct> orderProducts = new HashSet<>();

    @OneToOne
    ProductDiscount discount;

    public ProductDto toDto(){
        return new ProductDto(this.id, this.name, this.brand, this.price);
    }

}
