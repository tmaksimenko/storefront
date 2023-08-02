package com.tmaksimenko.storefront.model.discount;

import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import com.tmaksimenko.storefront.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "generaldiscounts")
@SuperBuilder(toBuilder = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class GeneralDiscount extends Discount {

    @Enumerated(EnumType.STRING)
    Role role;

    @Override
    public DiscountDto toDto () {
        return super.toDto().toBuilder().role(this.role.name()).type(DiscountType.ROLE).build();
    }
}
