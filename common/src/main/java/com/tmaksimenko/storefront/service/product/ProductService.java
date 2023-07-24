package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.dto.ProductCreateDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.model.Cart;
import com.tmaksimenko.storefront.model.Product;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();

    Optional<Product> findById(Long id);

    Cart createCart (CartDto cartDto, String username);

    Cart createCart (CartDto cartDto);

    ResponseEntity<String> createProduct (ProductCreateDto productCreateDto);

    ResponseEntity<String> updateProduct (Long id, ProductCreateDto productCreateDto);

    ResponseEntity<String> deleteProduct (Long id);

}
