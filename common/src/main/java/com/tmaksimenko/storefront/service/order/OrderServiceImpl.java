package com.tmaksimenko.storefront.service.order;

import com.tmaksimenko.storefront.dto.order.OrderDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.OrderNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderServiceImpl implements OrderService {

    final OrderRepository orderRepository;

    final AccountService accountService;
    final ProductService productService;

    @Override
    public List<Order> findAll () {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public ResponseEntity<String> createOrder (OrderDto orderDto) {

        Order order = Order.builder()
                .audit(Audit.builder().createdOn(LocalDateTime.now()).createdBy(orderDto.getUsername()).build())
                .account(accountService.findByUsername(orderDto.getUsername())
                        .orElseThrow(AccountNotFoundException::new))
                .payment(orderDto.getPaymentCreateDto().toPayment()).build();

        orderDto.getProductCreateDtos().forEach(
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
        orderRepository.flush();
        return new ResponseEntity<>("ORDER DELETED", HttpStatus.OK);
    }

    @Override
    public List<Order> findByLogin(String login) {
        Account account = accountService.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        return orderRepository.findByAccountId(account.getId());
    }

}
