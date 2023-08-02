package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderServiceImplementation implements OrderService {

    final OrderRepository orderRepository;

    final AccountService accountService;
    final ProductService productService;

    @Override
    public List<OrderGetDto> findAll () {
        return orderRepository.findAll().stream().map(Order::toFullDto).collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order cartToOrder () {
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

        return orderRepository.save(order);
    }

    @Override
    public Order createOrder (CartDto cartDto, String username) {

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

        return orderRepository.save(order);
    }

    @Override
    public Order deleteOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            orderRepository.delete(order.get());
            return order.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND", new OrderNotFoundException());
    }

    @Override
    public List<Order> findByLogin(String login) {
        Account account = accountService.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        return orderRepository.findByAccountId(account.getId());
    }

}
