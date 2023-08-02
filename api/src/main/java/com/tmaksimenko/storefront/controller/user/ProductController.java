package com.tmaksimenko.storefront.controller.user;

import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.service.account.AccountService;
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

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Tag(name = "User Operations")
@RestController
@EnableCaching
@CacheConfig(cacheNames = "products")
@RequestMapping("/products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductController {

    final AccountService accountService;
    final ProductService productService;

    @Operation(summary = "Fetch all products")
    @Cacheable("products")
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> findAll() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDtos = products.stream().map(Product::toDto).toList();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @Operation(summary = "Fetch individual product")
    @GetMapping("/view")
    public ResponseEntity<Product> viewProduct (@RequestParam Long id) {
        return ResponseEntity.of(productService.findById(id));
    }

    @Operation(summary = "View cart", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @GetMapping("/cart")
    public ResponseEntity<Cart> viewCart () {
        return new ResponseEntity<>(
                accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                        .orElseThrow(AccountNotFoundException::new).getCart(),
                HttpStatus.OK);
    }

    @Operation(summary = "Create a new cart (replaces previous)", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @PostMapping("/cart")
    public ResponseEntity<String> createCart (@RequestBody CartDto cartDto) {
        Account account = accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication()
                .getName()).orElseThrow(AccountNotFoundException::new);
        if (!isEmpty(account.getCart()))
            deleteCart();
        account.setCart(productService.createCart(cartDto));
        return new ResponseEntity<>("CART CREATED", HttpStatus.CREATED);
    }

    @Operation(summary = "Delete the cart", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/cart")
    public ResponseEntity<String> deleteCart () {
        Account account = accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (isEmpty(account.getCart())) {
            return new ResponseEntity<>("NO CART FOUND", HttpStatus.OK);
        } else {
            account.setCart(new Cart());
            return new ResponseEntity<>("CART DELETED", HttpStatus.OK);
        }
    }


}
