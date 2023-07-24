package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.dto.ProductCreateDto;
import com.tmaksimenko.storefront.dto.ProductDto;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administrator Utilities")
@RestController
@RequestMapping("/products")
@PreAuthorize("hasRole('ADMIN')")
@EnableCaching
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminProductController {

    final ProductService productService;

    @Operation(summary = "Fetch all products")
    @Cacheable("products")
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> findAll() {
        List<Product> products = productService.findAll();
        List<ProductDto> productDtos = products.stream().map(Product::toDto).toList();
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @Operation(summary = "Fetch individual product")
    @GetMapping("/view")
    public ResponseEntity<Product> viewProduct (@RequestParam Long id) {
        return ResponseEntity.of(productService.findById(id));
    }

    @Operation(summary = "Create product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @PostMapping("/add")
    public ResponseEntity<String> createProduct (@RequestBody ProductCreateDto productCreateDto) {
        return productService.createProduct(productCreateDto);
    }

    @Operation(summary = "Update product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @PutMapping("/update")
    public ResponseEntity<String> updateProduct (@RequestParam Long id, @RequestBody ProductCreateDto productCreateDto) {
        return productService.updateProduct(id, productCreateDto);
    }

    @Operation(summary = "Delete product", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteProduct (@RequestParam long id) {
        return productService.deleteProduct(id);
    }

}
