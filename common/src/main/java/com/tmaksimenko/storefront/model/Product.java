package com.tmaksimenko.storefront.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.model.OrderProduct.OrderProduct;
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

    float price;

    double weight;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<OrderProduct> orderProducts = new HashSet<>();

    public ProductDto toDto(){
        return new ProductDto(this.id, this.name, this.brand, this.price);
    }

}
