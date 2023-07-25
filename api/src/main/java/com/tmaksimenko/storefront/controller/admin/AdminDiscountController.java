package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.dto.discount.DiscountCreateDto;
import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.exception.DiscountNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Administrator Utilities")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@EnableCaching
@CacheConfig(cacheNames = "discounts")
@RequestMapping("/admin/discounts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminDiscountController {

    final ProductService productService;
    final DiscountService discountService;

    @Operation(summary = "View all discounts", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @GetMapping("/all")
    public ResponseEntity<List<DiscountDto>> viewAll() {
        List<? super Discount> discounts = discountService.findAllDiscounts();

        List<DiscountDto> discountDtos = discounts.stream().map(x -> {
            if (x.getClass().equals(GeneralDiscount.class))
                return ((GeneralDiscount) x).toDto();
            if (x.getClass().equals(ProductDiscount.class))
                return ((ProductDiscount) x).toDto();
            return ((Discount) x).toDto();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(discountDtos);
    }

    @Operation(summary = "View a specific discount", parameters = {
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Auth-Token",
                    required = true,
                    description = "JWT Token, can be generated in auth controller /auth")})
    @Cacheable
    @GetMapping("/view")
    public ResponseEntity<DiscountDto> viewDiscount (@RequestParam Long id) {
        Optional<? extends Discount> optionalDiscount = discountService.findById(id);
        if (optionalDiscount.isPresent())
            return ResponseEntity.ok(optionalDiscount.get().toDto());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DISCOUNT NOT FOUND", new DiscountNotFoundException());
    }


    @Operation(summary = "Create a discount", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PostMapping("/add")
    public ResponseEntity<DiscountDto> createDiscount (DiscountCreateDto discountCreateDto) {
        Audit audit = Audit.builder().createdOn(LocalDateTime.now()).createdBy(
                SecurityContextHolder.getContext().getAuthentication().getName()).build();

        if (ObjectUtils.isEmpty(discountCreateDto.getProductId())) {
            GeneralDiscount discount = GeneralDiscount.builder()
                    .role(discountCreateDto.getRole())
                    .audit(audit)
                    .percent(discountCreateDto.getPercent()).build();
            return ResponseEntity.ok(discountService.createDiscount(discount).toDto());

        } else try {
            ProductDiscount discount = ProductDiscount.builder()
                    .product(productService.findById(discountCreateDto.getProductId()).orElseThrow(ProductNotFoundException::new))
                    .audit(audit)
                    .percent(discountCreateDto.getPercent()).build();
            return ResponseEntity.ok(discountService.createDiscount(discount).toDto());

        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", e);
        }
    }

    @Operation(summary = "Delete discount", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<Discount> removeDiscount (@RequestParam Long id) {
        try {
            return ResponseEntity.ok(discountService.deleteDiscount(id));
        } catch (DiscountNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount NOT FOUND", e);
        }
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }
}



