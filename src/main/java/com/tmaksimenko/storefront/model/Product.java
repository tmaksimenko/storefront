package com.tmaksimenko.storefront.model;

import com.tmaksimenko.storefront.dto.ProductDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Table(name="products")
@EqualsAndHashCode(exclude = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    long id;

    String name;

    String brand;

    float price;

    double originlat;

    double originlong;

    @ManyToMany(mappedBy = "products", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Order> orders;

    public ProductDto toDto(){
        return new ProductDto(this.id, this.name, this.brand, this.price);
    }

}
