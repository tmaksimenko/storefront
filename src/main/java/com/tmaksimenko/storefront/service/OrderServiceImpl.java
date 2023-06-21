package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.OrderCreateDto;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.repository.AccountRepository;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

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

        Set<Product> products = new HashSet<>();

        for (Long productId : orderCreateDto.getProductIds()) {
            Optional<Product> optionalProduct = productRepository.findById(String.valueOf(productId));
            if (optionalProduct.isEmpty()) return "FAILURE";
            products.add(optionalProduct.get());
        }

        order.setProducts(products);

        orderRepository.save(order);

        return "SUCCESS";
    }

}
