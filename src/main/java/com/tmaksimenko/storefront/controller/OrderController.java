package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.service.order.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> viewAll() {
        List<Order> orders = orderService.findAll();
        List<OrderDto> orderDtos = orders.stream().map(Order::toDto).toList();

        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateDto orderCreateDto) {
        try {
            return orderService.createOrder(orderCreateDto);
            } catch (AccountNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", e);
            } catch (ProductNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> removeOrder(@RequestParam Long id) {
        try {
            return orderService.deleteOrder(id);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", e);
        }
    }

}
