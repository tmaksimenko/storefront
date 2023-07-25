package com.tmaksimenko.storefront.service.product;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.payment.Payment;
import com.tmaksimenko.storefront.repository.ProductRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;

    final DiscountService discountService;
    final AccountService accountService;

    @Override
    public List<Product> findAll () {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Cart createCart (CartDto cartDto, String username) {
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(Collectors.toMap(
                CartItemDto::getProductId, CartItemDto::getQuantity));

        List<GeneralDiscount> personalDiscounts =
                discountService.findByRole(
                        accountService.findByUsername(username).orElseThrow(AccountNotFoundException::new)
                    .getRole());

        GeneralDiscount largestDiscount = GeneralDiscount.builder().percent(0.0).build();
        for (GeneralDiscount personalDiscount : personalDiscounts)
            if (personalDiscount.getPercent() > largestDiscount.getPercent())
                largestDiscount = personalDiscount;

        final GeneralDiscount personalDiscount = largestDiscount;

        double price = cartDto.getCartItemDtos().stream().mapToDouble(x -> {
            Product product = this.findById(x.getProductId()).orElseThrow(ProductNotFoundException::new);
            Optional<ProductDiscount> discount = discountService.findByProductId(x.getProductId());
            if (discount.isEmpty())
                return product.getPrice() * ((double) x.getQuantity()) + product.getWeight() * 0.1;
            return ((product.getPrice() * ((double) x.getQuantity())
                    * (100.0 - discountService.findByProductId(x.getProductId()).get().getPercent()))
                    + product.getWeight() * 0.1)
                    * (100.0 - personalDiscount.getPercent());
        }).sum();


        Payment payment = cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID);

        return Cart.builder().price(price).items(itemMap).payment(payment).build();
    }

    @Override
    public Cart createCart (CartDto cartDto) {
        return createCart(cartDto, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public Product createProduct (ProductCreateDto productCreateDto) {
        Product product = Product.builder()
                .name(productCreateDto.getName())
                .brand(productCreateDto.getBrand())
                .price(productCreateDto.getPrice())
                .weight(productCreateDto.getWeight()).build();
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct (Long id, ProductCreateDto productCreateDto) {
        if (productRepository.findById(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", new ProductNotFoundException());

        Product product = productRepository.findById(id).get();

        if (ObjectUtils.isNotEmpty(productCreateDto.getName()))
            product.setName(productCreateDto.getName());

        if (ObjectUtils.isNotEmpty(productCreateDto.getBrand()))
            product.setBrand(productCreateDto.getBrand());

        if (ObjectUtils.isNotEmpty(productCreateDto.getPrice()))
            product.setPrice(productCreateDto.getPrice());

        if (ObjectUtils.isNotEmpty(productCreateDto.getWeight()))
            product.setWeight(productCreateDto.getWeight());

        return product;
    }

    @Override
    public Product deleteProduct (Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND", new ProductNotFoundException());

        productRepository.delete(product.get());

        return product.get();
    }

}
