package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;

import java.util.List;
import java.util.Optional;

public interface DiscountService {
    List<? super Discount> findAllDiscounts ();

    Optional<? extends Discount> findById (Long id);

    Optional<ProductDiscount> findByProductId (Long id);

    List<GeneralDiscount> findByRole (Role role);

    GeneralDiscount createDiscount (GeneralDiscount discount);

    ProductDiscount createDiscount (ProductDiscount discount);

    Discount deleteDiscount (Long id);

}
