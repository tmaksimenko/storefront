package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.model.Cart;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Transactional
@EnableCaching
@CacheConfig(cacheNames = "orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderServiceImpl implements OrderService {

    final OrderRepository orderRepository;

    final AccountService accountService;
    final ProductService productService;

    @Override
    public List<Order> findAll () {
        return orderRepository.findAll();
    }

    @Cacheable
    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public ResponseEntity<String> cartToOrder () {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> optionalAccount = accountService.findByUsername(username);
        if (optionalAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());
        if (isEmpty(optionalAccount.get().getCart()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CART IS EMPTY");

        Cart cart = optionalAccount.get().getCart();

        Order order = Order.builder()
                .account(optionalAccount.get())
                .audit(Audit.builder()
                        .createdOn(LocalDateTime.now())
                        .createdBy(username).build())
                .payment(cart.getPayment()).build();

        cart.getItems().forEach(
                (key, value) -> order.addProduct(
                        productService.findById(key).orElseThrow(ProductNotFoundException::new), value));

        orderRepository.save(order);
        return new ResponseEntity<>("ORDER CREATED", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> createOrder (CartDto cartDto, String username) {

        Order order = Order.builder()
                .audit(Audit.builder().createdOn(LocalDateTime.now()).createdBy(username).build())
                .account(accountService.findByUsername(username)
                        .orElseThrow(AccountNotFoundException::new))
                .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.PAID)).build();

        cartDto.getCartItemDtos().forEach(
                x -> order.addProduct(
                        productService.findById(x.getProductId())
                                .orElseThrow(ProductNotFoundException::new),
                        x.getQuantity()));

        orderRepository.save(order);

        return new ResponseEntity<>("ORDER CREATED", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteOrder(Long id) throws OrderNotFoundException {
        orderRepository.deleteById(id);
        return new ResponseEntity<>("ORDER DELETED", HttpStatus.OK);
    }

    @Cacheable
    @Override
    public List<Order> findByLogin(String login) {
        Account account = accountService.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        return orderRepository.findByAccountId(account.getId());
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
