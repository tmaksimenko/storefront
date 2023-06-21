package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.OrderCreateDto;
import com.tmaksimenko.storefront.dto.OrderDto;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> findAll() {
        List<Order> orders = orderService.findAll();
        List<OrderDto> orderDtos = orders.stream().map(Order::toDto).toList();
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAccount(@RequestBody OrderCreateDto orderCreateDto) {
        String result = orderService.createOrder(orderCreateDto);
        if (result.equals("SUCCESS")) return new ResponseEntity<> (result, HttpStatus.CREATED);
        return new ResponseEntity<> (result, HttpStatus.FORBIDDEN);
    }

}
