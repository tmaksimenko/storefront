package com.tmaksimenko.storefront.model.discount;

import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.model.base.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Discount extends BaseEntity {

    Double percent;

    public DiscountDto toDto () {
        return DiscountDto.builder().percent(percent).build();
    }
}
