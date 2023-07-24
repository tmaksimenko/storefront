package com.tmaksimenko.storefront.service.product;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
