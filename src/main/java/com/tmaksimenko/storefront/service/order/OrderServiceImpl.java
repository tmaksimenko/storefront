package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.repository.AccountRepository;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
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

        Order order = new Order(true);

        List<Account> accounts = accountRepository.findByUsername(orderCreateDto.getUsername());
        if (accounts.size() != 1) return "FAILURE";
        order.setAccount(accounts.get(0));

//        Set<Product> products = orderCreateDto.getProductIds().stream().map(
//                x -> productRepository.findById(String.valueOf(x)).orElseThrow(EntityNotFoundException::new)
//        ).collect(Collectors.toSet());
//
//        order.setOrderProducts(products);

        orderCreateDto.getProductIdsWithQuantities().forEach(
                x -> order.addProduct(productRepository.findById(String.valueOf(x.getProductId()))
                    .orElseThrow(EntityNotFoundException::new), x.getQuantity()));

        orderRepository.save(order);

        return "SUCCESS";
    }

}
