package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderGetDto> findAll();

    Optional<Order> findById(Long id);

    Order cartToOrder ();

    @SuppressWarnings("unused")
    Order createOrder(CartDto cartDto, String username);

    Order deleteOrder(Long id);

    List<Order> findByLogin(String login);

}
