package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
}
