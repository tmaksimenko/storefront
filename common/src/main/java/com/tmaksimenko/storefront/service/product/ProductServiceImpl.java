package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Cart;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.payment.Payment;
import com.tmaksimenko.storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;

    @Override
    public List<Product> findAll () {
        return productRepository.findAll();
    }

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
            return product.getPrice() * ((double) x.getQuantity()) + product.getWeight() * 0.1;
        }).sum();

        Payment payment = cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID);

        return Cart.builder().price(price).items(itemMap).payment(payment).build();
    }

}
