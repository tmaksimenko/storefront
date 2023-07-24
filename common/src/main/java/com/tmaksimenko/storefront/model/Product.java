package com.tmaksimenko.storefront.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.dto.ProductDto;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="products")
@EqualsAndHashCode(exclude = "orderProducts")
public class Product {
    @Id
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
