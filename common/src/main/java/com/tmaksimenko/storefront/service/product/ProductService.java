package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();

    Optional<Product> findById(Long id);

}
