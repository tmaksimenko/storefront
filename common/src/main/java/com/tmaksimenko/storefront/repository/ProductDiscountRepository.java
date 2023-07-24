package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
    Optional<ProductDiscount> findByProductId (long id);
}
