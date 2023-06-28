package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.model.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    List<Order> findAll();

    ResponseEntity<String> createOrder(OrderCreateDto orderCreateDto);

    ResponseEntity<String> deleteOrder(Long id);

}
