package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.annotation.ExcludeFromJacocoGeneratedReport;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Administrator Utilities")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@EnableCaching
@CacheConfig(cacheNames = "orders")
@RequestMapping("/admin/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminOrderController {

    final OrderService orderService;
    final ProductService productService;

    @Operation(summary = "View all orders", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @GetMapping("/all")
    public ResponseEntity<List<OrderGetDto>> viewAll() {
        List<OrderGetDto> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "View a specific order", parameters = {
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Auth-Token",
                    required = true,
                    description = "JWT Token, can be generated in auth controller /auth")})
    @Cacheable("orders")
    @GetMapping("/view")
    public ResponseEntity<OrderGetDto> viewOrderDetails(@RequestParam Long id) {
        Optional<Order> optionalOrder = orderService.findById(id);
        if (optionalOrder.isPresent())
            return ResponseEntity.ok(optionalOrder.get().toFullDto());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());
    }


    @Operation(summary = "Place an order", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable("orders")
    @PostMapping("/create")
    public ResponseEntity<OrderGetDto> createOrder(@RequestParam String username, @RequestBody CartDto cartDto) {
        return ResponseEntity.ok(orderService.createOrder(cartDto, username).toFullDto());
    }

    @Operation(summary = "Update order items", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable("orders")
    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@RequestParam Long id, @RequestBody Map<String, Integer> params) {
        Optional<Order> optionalOrder = orderService.findById(id);
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());

        Order order = optionalOrder.get();

        Map<Long, Integer> products;
        products = params.entrySet().stream().collect(Collectors.toMap(
                (param) -> Long.valueOf(param.getKey()),
                Map.Entry::getValue));

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

    @Operation(summary = "Delete an order", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<OrderGetDto> removeOrder(@RequestParam Long id) {
        return ResponseEntity.ok(orderService.deleteOrder(id).toFullDto());
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    @ExcludeFromJacocoGeneratedReport
    public void emptyCache () {
    }
}



