package com.tmaksimenko.storefront.controller.user;

import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "User Operations")
@RestController
@EnableCaching
@CacheConfig(cacheNames = "orders")
@RequestMapping("/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderController {

    final AccountService accountService;
    final OrderService orderService;
    final ProductService productService;

    @Operation(
            summary = "Views all orders on your account",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @GetMapping("/viewall")
    public ResponseEntity<List<OrderGetDto>> viewAll() {
        List<Order> orders = orderService.findByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        List<OrderGetDto> orderGetDtos = orders.stream().map(Order::toFullDto).toList();
        return new ResponseEntity<>(orderGetDtos, HttpStatus.OK);
    }

    @Operation(summary = "View a specific order from your account", parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")})
    @Cacheable("orders")
    @GetMapping("/view")
    public ResponseEntity<OrderGetDto> viewOrderDetails(@RequestParam Long id) {
        Optional<Order> optionalOrder = orderService.findById(id);

        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());

        Optional<Account> optionalAccount = accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if (optionalAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());

        if (!optionalAccount.get()
                .getOrders().contains(optionalOrder.get()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ORDER NOT YOURS");

        return new ResponseEntity<>(optionalOrder.get().toFullDto(), HttpStatus.OK);
    }

    @Operation(summary = "Place an order from the cart", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PostMapping("/submit")
    public ResponseEntity<OrderGetDto> submitOrder () {
        return ResponseEntity.ok(orderService.cartToOrder().toFullDto());
    }

    @Operation(summary = "Update order items", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@RequestParam Long id, @RequestBody Map<String, Integer> params) {
        Optional<Order> optionalOrder = orderService.findById(id);
        if (optionalOrder.isEmpty())
            return new ResponseEntity<>("ORDER NOT FOUND", HttpStatus.NOT_FOUND);

        if (!accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get()
                .getOrders().contains(optionalOrder.get()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ORDER NOT YOURS");

        Order order = optionalOrder.get();

        Map<Long, Integer> products;
        try {
            products = params.entrySet().stream().collect(Collectors.toMap(
                    (param) -> Long.valueOf(param.getKey()),
                    Map.Entry::getValue));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "INVALID PRODUCT ID", e);
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

        return ResponseEntity.ok(String.format(
                "UPDATED PRODUCTS -> %s ADDED PRODUCTS -> %s NOT FOUND -> %s",
                updatedProductIds, addedProductIds, notFoundProductIds));
    }

    @Operation(summary = "Delete order", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<Order> removeOrder(@RequestParam Long id) {
        if (! accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(AccountNotFoundException::new)
                .getOrders().contains(orderService.findById(id).orElseThrow(OrderNotFoundException::new)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ORDER NOT YOURS");
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }

}
