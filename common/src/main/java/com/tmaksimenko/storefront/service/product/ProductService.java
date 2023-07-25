package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.Product;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();

    Optional<Product> findById(Long id);

    Cart createCart (CartDto cartDto, String username);

    Cart createCart (CartDto cartDto);

    Product createProduct (ProductCreateDto productCreateDto);

    Product updateProduct (Long id, ProductCreateDto productCreateDto);

    Product deleteProduct (Long id);

}
