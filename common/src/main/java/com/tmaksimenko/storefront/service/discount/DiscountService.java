package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;

import java.util.List;
import java.util.Optional;

public interface DiscountService {
    List<? super Discount> findAllDiscounts ();

    Optional<ProductDiscount> findByProductId (Long id);

}
