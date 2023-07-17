package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.model.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> findAll();

    Optional<Order> findById(Long id);

    ResponseEntity<String> createOrder(OrderCreateDto orderCreateDto);

    ResponseEntity<String> deleteOrder(Long id);

}
