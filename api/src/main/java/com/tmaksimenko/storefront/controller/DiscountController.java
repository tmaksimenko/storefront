package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.discount.DiscountCreateDto;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.repository.GeneralDiscountRepository;
import com.tmaksimenko.storefront.repository.ProductDiscountRepository;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Administrator Utilities")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/discounts")
public class DiscountController {

    final GeneralDiscountRepository generalDiscountRepository;
    final ProductDiscountRepository productDiscountRepository;
    final DiscountService discountService;
    final ProductService productService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(
            summary = "View discounts",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<? super Discount>> viewAll() {
        return new ResponseEntity<>(discountService.findAllDiscounts(), HttpStatus.OK);
    }

    @Operation(
            summary = "Test",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test")
    public ResponseEntity<List<ProductDiscount>> test() {
        logger.info("Discount Products: {}", productDiscountRepository.findAll().stream().map(ProductDiscount::getProduct).collect(Collectors.toList()));
        return new ResponseEntity<>(productDiscountRepository.findAll(), HttpStatus.OK);
    }

    @Operation(
            summary = "Add discount",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> add (DiscountCreateDto discountCreateDto) {
        Audit audit = Audit.builder().createdOn(LocalDateTime.now()).createdBy(
                SecurityContextHolder.getContext().getAuthentication().getName()).build();
        if (ObjectUtils.isEmpty(discountCreateDto.getProductId())) {
            GeneralDiscount discount = GeneralDiscount.builder().audit(audit).percent(discountCreateDto.getPercent()).role(discountCreateDto.getRole()).build();
            generalDiscountRepository.save(discount);
        } else try {
            ProductDiscount discount = ProductDiscount.builder()
                    .product(productService.findById(discountCreateDto.getProductId()).orElseThrow(ProductNotFoundException::new))
                    .audit(audit)
                    .percent(discountCreateDto.getPercent()).build();
            productDiscountRepository.save(discount);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", e);
        }
        return new ResponseEntity<>("SUCCESS", HttpStatus.CREATED);
    }
}
