package com.tmaksimenko.storefront.controller.admin;
/*

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Administrator Utilities")
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminOrderController {

    final OrderService orderService;
    final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderGetDto>> viewAll() {
        List<Order> orders = orderService.findAll();
        List<OrderGetDto> orderGetDtos = orders.stream().map(Order::toFullDto).toList();
        return new ResponseEntity<>(orderGetDtos, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<OrderGetDto> viewOrderDetails(Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent())
            return new ResponseEntity<>(order.get().toFullDto(), HttpStatus.OK);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());
    }


    */
/*@Operation(
            summary = "Places an order (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")// or (#orderCreateDto.username == authentication.name)")
    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestParam String username, @RequestBody OrderCreateDto orderCreateDto) {
        try {
            return orderService.createOrder(orderCreateDto.toOrderDto(username));
            } catch (AccountNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", e);
            } catch (ProductNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", e);
        }
    }
*//*

    @Operation(
            summary = "Updates an order (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
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

    @Operation(
            summary = "Deletes an order (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<String> removeOrder(@RequestParam Long id) {
        try {
            return orderService.deleteOrder(id);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", e);
        }
    }

}
*/
