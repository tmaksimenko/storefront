package com.tmaksimenko.storefront.model.discount;

import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "generaldiscounts")
@SuperBuilder
@Data
@NoArgsConstructor
public class GeneralDiscount extends Discount {

    String role;

    @Override
    public DiscountDto toDto () {
        return super.toDto().toBuilder().role(this.role).type(DiscountType.ROLE).build();
    }
}
