package com.tmaksimenko.storefront.dto.discount;

import com.tmaksimenko.storefront.dto.ProductDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DiscountDto {

    double percent;

    String role;

    ProductDto product;

    DiscountType type;

}
