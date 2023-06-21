package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;

    @Override
    public List<Product> findAll () {
        return productRepository.findAll();
    }
}
