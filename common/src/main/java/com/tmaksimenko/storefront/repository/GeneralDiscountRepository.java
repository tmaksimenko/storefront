package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralDiscountRepository extends JpaRepository<GeneralDiscount, Long> {
}
