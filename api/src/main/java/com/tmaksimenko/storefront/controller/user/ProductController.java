package com.tmaksimenko.storefront.controller.user;

import com.tmaksimenko.storefront.dto.ProductDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Cart;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Tag(name = "User Operations")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductController {

    final AccountService accountService;
    final ProductService productService;

    @Operation(summary = "Fetches list of all products")
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> findAll() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDtos = products.stream().map(Product::toDto).toList();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @Operation(summary = "Fetches individual product")
    @GetMapping("/view")
    public ResponseEntity<Product> viewProduct (@RequestParam Long id) {
        return ResponseEntity.of(productService.findById(id));
    }

    @Operation(summary = "Views the cart")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart")
    public ResponseEntity<Cart> viewCart () {
        return new ResponseEntity<>(
                accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                        .orElseThrow(AccountNotFoundException::new).getCart(),
                HttpStatus.OK);
    }

    @Operation(summary = "Creates a new cart or replaces the previous cart")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart")
    public ResponseEntity<String> createCart (@RequestBody CartDto cartDto) {
        Account account = accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication()
                .getName()).orElseThrow(AccountNotFoundException::new);
        if (!isEmpty(account.getCart()))
            deleteCart();
        account.setCart(productService.createCart(cartDto));
        return new ResponseEntity<>("CART CREATED", HttpStatus.CREATED);
    }

    @Operation(summary = "Deletes the current cart")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cart")
    public ResponseEntity<String> deleteCart () {
        @SuppressWarnings("all")
        Account account = accountService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (isEmpty(account.getCart())) {
            return new ResponseEntity<>("NO CART FOUND", HttpStatus.OK);
        } else {
            account.setCart(new Cart());
            return new ResponseEntity<>("CART DELETED", HttpStatus.OK);
        }
    }


}
