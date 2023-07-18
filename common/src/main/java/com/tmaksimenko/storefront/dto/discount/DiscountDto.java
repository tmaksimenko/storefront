package com.tmaksimenko.storefront.dto.discount;

import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder(toBuilder = true)
public class DiscountDto {
    double percent;
    String role;
    Set<ProductDto> products;
    DiscountType type;
}
