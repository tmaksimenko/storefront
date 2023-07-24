package com.tmaksimenko.storefront.service.discount;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface DiscountService {
    List<? super Discount> findAllDiscounts ();

    Optional<? extends Discount> findById (Long id);

    Optional<ProductDiscount> findByProductId (Long id);

    List<GeneralDiscount> findByRole (Role role);

    ResponseEntity<String> createDiscount (GeneralDiscount discount);

    ResponseEntity<String> createDiscount (ProductDiscount discount);

    ResponseEntity<String> deleteDiscount (Long id);

}
