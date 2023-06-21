package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.model.OrderProduct.OrderProduct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Data
@Table(name="products")
@EqualsAndHashCode(exclude = "orderProducts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    long id;

    String name;

    String brand;

    float price;

    double originlat;

    double originlong;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Set<OrderProduct> orderProducts;

    public ProductDto toDto(){
        return new ProductDto(this.id, this.name, this.brand, this.price);
    }

}
