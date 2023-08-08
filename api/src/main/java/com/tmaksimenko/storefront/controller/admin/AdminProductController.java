package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.annotation.ExcludeFromJacocoGeneratedReport;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.model.Product;
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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Administrator Utilities")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@EnableCaching
@CacheConfig(cacheNames = "products")
@RequestMapping("/admin/products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminProductController {

    final ProductService productService;

    @Operation(summary = "Create product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PostMapping("/add")
    public ResponseEntity<Product> createProduct (@RequestBody ProductCreateDto productCreateDto) {
        return ResponseEntity.ok(productService.createProduct(productCreateDto));
    }

    @Operation(summary = "Update product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PutMapping("/update")
    public ResponseEntity<Product> updateProduct (@RequestParam Long id, @RequestBody ProductCreateDto productCreateDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productCreateDto));
    }

    @Operation(summary = "Delete product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<Product> deleteProduct (@RequestParam long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    @ExcludeFromJacocoGeneratedReport
    public void emptyCache () {
    }

}
