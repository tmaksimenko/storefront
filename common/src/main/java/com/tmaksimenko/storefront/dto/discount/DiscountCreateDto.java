package com.tmaksimenko.storefront.dto.discount;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DiscountCreateDto {

    double percent;

    String role;

    Long productId;
}
