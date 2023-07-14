package com.tmaksimenko.storefront.model.Discount;

import com.tmaksimenko.storefront.dto.DiscountDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.EnumSet;

@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BaseDiscount extends BaseEntity {

    double percent;

    EnumSet<Role> roles;

    public DiscountDto toDto () {
        return DiscountDto.builder().id(this.getId()).percent(percent).roles(roles).build();
    }
}
