package com.tmaksimenko.storefront.dto.discount;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class DiscountCreateDto {
    double percent;
    String role;
    List<Long> products;
}
