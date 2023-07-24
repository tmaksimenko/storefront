package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.dto.ProductCreateDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Cart;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.payment.Payment;
import com.tmaksimenko.storefront.repository.ProductRepository;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@EnableCaching
@CacheConfig(cacheNames = "products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;

    final DiscountService discountService;

    @Override
    public List<Product> findAll () {
        return productRepository.findAll();
    }

    @Cacheable("products")
    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Cart createCart (CartDto cartDto) {
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(Collectors.toMap(
                CartItemDto::getProductId, CartItemDto::getQuantity));

        double price = cartDto.getCartItemDtos().stream().mapToDouble(x -> {
            Product product = this.findById(x.getProductId()).orElseThrow(ProductNotFoundException::new);
            Optional<ProductDiscount> discount = discountService.findByProductId(x.getProductId());
            if (discount.isEmpty())
                return product.getPrice() * ((double) x.getQuantity()) + product.getWeight() * 0.1;
            return (product.getPrice() * ((double) x.getQuantity()) * discountService
                            .findByProductId(x.getProductId()).get().getPercent())
                            + product.getWeight() * 0.1;
        }).sum();


        Payment payment = cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID);

        return Cart.builder().price(price).items(itemMap).payment(payment).build();
    }

    @Override
    public ResponseEntity<String> createProduct (ProductCreateDto productCreateDto) {
        Product product = Product.builder()
                .name(productCreateDto.getName())
                .brand(productCreateDto.getBrand())
                .price(productCreateDto.getPrice())
                .weight(productCreateDto.getWeight()).build();
        productRepository.save(product);
        return new ResponseEntity<>("PRODUCT CREATED", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> updateProduct (Long id, ProductCreateDto productCreateDto) {

        Product product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);

        if (!ObjectUtils.isEmpty(productCreateDto.getName()))
            product.setName(productCreateDto.getName());

        if (!ObjectUtils.isEmpty(productCreateDto.getBrand()))
            product.setBrand(productCreateDto.getBrand());

        if (!ObjectUtils.isEmpty(productCreateDto.getPrice()))
            product.setPrice(productCreateDto.getPrice());

        if (!ObjectUtils.isEmpty(productCreateDto.getWeight()))
            product.setWeight(productCreateDto.getWeight());

        return new ResponseEntity<>("PRODUCT UPDATED", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteProduct (Long id) {

        productRepository.deleteById(id);

        return new ResponseEntity<>("PRODUCT DELETED", HttpStatus.OK);
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
