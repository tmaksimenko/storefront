package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.ProductDto;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> findAll() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDtos = products.stream().map(Product::toDto).toList();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }


}