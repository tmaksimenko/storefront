package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralDiscountRepository extends JpaRepository<GeneralDiscount, Long> {
    List<GeneralDiscount> findByRole (Role role);
}
