package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.model.discount.Discount;

import java.util.List;

public interface DiscountService {
    List<? super Discount> findAllDiscounts ();
}
