package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.OrderCreateDto;
import com.tmaksimenko.storefront.model.Order;

import java.util.List;

public interface OrderService {
    List<Order> findAll();

    String createOrder(OrderCreateDto orderCreateDto);

}
