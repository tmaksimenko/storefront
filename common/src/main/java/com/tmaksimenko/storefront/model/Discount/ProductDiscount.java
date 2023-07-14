package com.tmaksimenko.storefront.model.Discount;

import com.tmaksimenko.storefront.dto.DiscountDto;
import com.tmaksimenko.storefront.model.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "discounts")
@SuperBuilder
@Data
@NoArgsConstructor
public class ProductDiscount extends BaseDiscount {

    @ManyToMany
    @JoinTable(
            name = "discount_product",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    Set<Product> products;

    @Override
    public DiscountDto toDto() {
        return super.toDto().toBuilder().products(
                products.stream().map(Product::toDto).collect(Collectors.toSet())).build();
    }

}
