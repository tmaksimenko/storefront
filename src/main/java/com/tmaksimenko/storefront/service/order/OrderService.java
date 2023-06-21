package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.model.Order;

import java.util.List;

public interface OrderService {
    List<Order> findAll();

    String createOrder(OrderCreateDto orderCreateDto);

}
