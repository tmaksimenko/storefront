package com.tmaksimenko.storefront.model.discount;

import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import com.tmaksimenko.storefront.model.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "productdiscounts")
@SuperBuilder
@Data
@NoArgsConstructor
public class ProductDiscount extends Discount {

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "discount_product",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    Set<Product> products;

    @Override
    public DiscountDto toDto() {
        return super.toDto().toBuilder().products(
                products.stream().map(Product::toDto).collect(Collectors.toSet())).type(DiscountType.PRODUCT).build();
    }

}
