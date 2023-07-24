package com.tmaksimenko.storefront.model.discount;

import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import com.tmaksimenko.storefront.model.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "productdiscounts")
@SuperBuilder
@Data
@NoArgsConstructor
public class ProductDiscount extends Discount {

    @OneToOne
    Product product;

    @Override
    public DiscountDto toDto() {
        return super.toDto().toBuilder().product(product.toDto()).type(DiscountType.PRODUCT).build();
    }

}
