package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.OrderCreateDto;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.repository.AccountRepository;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    final OrderRepository orderRepository;
    final AccountRepository accountRepository;
    final ProductRepository productRepository;

    @Override
    public List<Order> findAll () {
        return orderRepository.findAll();
    }

    @Override
    public String createOrder(OrderCreateDto orderCreateDto) {

        Order order = new Order();

        List<Account> accounts = accountRepository.findByUsername(orderCreateDto.getUsername());
        if (accounts.size() != 1) return "FAILURE";
        order.setAccount(accounts.get(0));

        Set<Product> products = orderCreateDto.getProductIds().stream().map(
                x -> productRepository.findById(String.valueOf(x)).orElseThrow(EntityNotFoundException::new)
        ).collect(Collectors.toSet());

        order.setProducts(products);

        orderRepository.save(order);

        return "SUCCESS";
    }

}
