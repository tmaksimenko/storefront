package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountId (Long accountId);
}
