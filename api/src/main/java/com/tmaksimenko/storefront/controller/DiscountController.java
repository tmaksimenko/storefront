package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.discount.DiscountCreateDto;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.repository.GeneralDiscountRepository;
import com.tmaksimenko.storefront.repository.ProductDiscountRepository;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/discounts")
public class DiscountController {

    final GeneralDiscountRepository generalDiscountRepository;
    final ProductDiscountRepository productDiscountRepository;
    final DiscountService discountService;
    final ProductService productService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/all")
    public ResponseEntity<List<? super Discount>> viewAll() {
        return new ResponseEntity<>(discountService.findAllDiscounts(), HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<List<ProductDiscount>> test() {
        logger.info("Discount Products: {}", productDiscountRepository.findAll().stream().map(ProductDiscount::getProducts).collect(Collectors.toList()));
        return new ResponseEntity<>(productDiscountRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> add (DiscountCreateDto discountCreateDto) {
        if (CollectionUtils.isEmpty(discountCreateDto.getProducts())) {
            GeneralDiscount discount = GeneralDiscount.builder().percent(discountCreateDto.getPercent()).role(discountCreateDto.getRole()).build();
            generalDiscountRepository.save(discount);
        } else try {
            ProductDiscount discount = ProductDiscount.builder().percent(discountCreateDto.getPercent()).build();
            Set<Product> products = discountCreateDto.getProducts().stream().map(
                    x -> productService.findById(x).orElseThrow(ProductNotFoundException::new)).collect(Collectors.toSet());
            discount.setProducts(products);
            productDiscountRepository.save(discount);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ONE OR MORE PRODUCTS NOT FOUND", e);
        }
        return new ResponseEntity<>("SUCCESS", HttpStatus.CREATED);
    }

}
