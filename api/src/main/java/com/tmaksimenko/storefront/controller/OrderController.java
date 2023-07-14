package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.order.OrderCreateDto;
import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.OrderProduct.OrderProduct;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;
    final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> viewAll() {
        List<Order> orders = orderService.findAll();
        List<OrderDto> orderDtos = orders.stream().map(Order::toFullDto).toList();
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<OrderDto> viewOrderDetails(Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent())
            return new ResponseEntity<>(order.get().toFullDto(), HttpStatus.OK);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());
    }

    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username.equals(#orderCreateDto.username)")
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

    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@RequestParam Long id, @RequestBody Map<String, Integer> params) {
        Optional<Order> optionalOrder = orderService.findById(id);
        if (optionalOrder.isEmpty())
            return new ResponseEntity<>("ORDER NOT FOUND", HttpStatus.NOT_FOUND);

        Order order = optionalOrder.get();

        Map<Long, Integer> products;
        try {
            products = params.entrySet().stream().collect(Collectors.toMap(
                    (param) -> Long.valueOf(param.getKey()),
                    Map.Entry::getValue));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "INVALID PRODUCT ID", ex);
        }

        Map<Long, Integer> currentProductIds = order.getOrderProducts().stream()
                .collect(Collectors.toMap(
                        (orderProduct) -> orderProduct.getProduct().getId(),
                        OrderProduct::getQuantity));

        List<Long> updatedProductIds = new ArrayList<>();
        List<Long> addedProductIds = new ArrayList<>();
        List<Long> notFoundProductIds = new ArrayList<>();

        products.forEach(
            (pid, q) -> {
                if (currentProductIds.containsKey(pid)) {
                    if (!Objects.equals(q, currentProductIds.get(pid))) {
                        order.changeProductQuantity(pid, q);
                        updatedProductIds.add(pid);
                    }
                } else try {
                    order.addProduct(
                            productService.findById(pid)
                                    .orElseThrow(ProductNotFoundException::new), q);
                    addedProductIds.add(pid);
                } catch (ProductNotFoundException e) {
                    notFoundProductIds.add(pid);
                }
            }
        );

        return new ResponseEntity<>(String.format(
                "UPDATED PRODUCTS -> %s ADDED PRODUCTS -> %s NOT FOUND -> %s",
                updatedProductIds, addedProductIds, notFoundProductIds), HttpStatus.OK);
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
