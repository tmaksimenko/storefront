package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.exception.ProductNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.model.orderProduct.OrderProduct;
import com.tmaksimenko.storefront.model.payment.ExpiryDate;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import com.tmaksimenko.storefront.repository.OrderRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.order.OrderServiceImplementation;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Mock
    OrderRepository orderRepository;
    
    @Mock
    AccountService accountService;
    
    @Mock
    ProductService productService;
    
    OrderService orderService;
    
    Account account;
    
    Product product;
    
    Order order;
    
    CartDto cartDto;

    Cart cart;

    @BeforeEach
    public void setup () {
        orderService = new OrderServiceImplementation(orderRepository, accountService, productService);
        Audit audit = new Audit("Test");
        account = Account.builder()
                .id(1L)
                .audit(audit)
                .username("testUser")
                .email("testEmail@mail.com")
                .password(passwordEncoder.encode("testPassword"))
                .role(Role.ROLE_USER)
                .address(Address.builder()
                        .streetAddress("1 Street St")
                        .country("Canada")
                        .postalCode("M1M1M1")
                        .build())
                .build();
        product = Product.builder()
                .id(1000L)
                .name("testName")
                .brand("testBrand")
                .price(10.0)
                .weight(1.0).build();
        cartDto = CartDto.builder()
                .paymentCreateDto(PaymentCreateDto.builder()
                        .paymentProvider(PaymentProvider.VISA)
                        .paymentInfo(PaymentInfo.builder()
                                .cardNumber(1111111111111111L)
                                .expiry(new ExpiryDate(11, 23))
                                .securityCode(111)
                                .postalCode(account.getAddress().getPostalCode())
                                .build())
                        .build())
                .cartItemDtos(List.of(new CartItemDto(product.getId())))
                .build();
        cart = Cart.builder()
                .price(product.getPrice() + (product.getWeight() * 0.1))
                .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID))
                .items(cartDto.getCartItemDtos().stream().collect(
                        Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity)))
                .build();
        order = Order.builder()
                .id(100L)
                .audit(audit)
                .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.PAID))
                .account(account)
                .build();
        order.getOrderProducts().add(new OrderProduct(order, product));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword()));
    }

    @Test
    @DisplayName("Successful findAll")
    public void test_successful_findAll () {
        //given
        Order order1 = order.toBuilder().id(101L).build();
        given(orderRepository.findAll()).willReturn(List.of(order, order1));

        // when
        List<Order> orders = orderService.findAll();

        // then
        assertThat(orders).hasSize(2).contains(order).contains(order1);
    }

    @Test
    @DisplayName("Failed findAll")
    public void test_failed_findAll () {
        //given
        given(orderRepository.findAll()).willReturn(List.of());

        // when
        List<Order> orders = orderService.findAll();

        // then
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("Successful findById")
    public void test_successful_findById () {
        //given
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        // when
        Optional<Order> order1 = orderService.findById(order.getId());

        // then
        assertThat(order1).isPresent().get().isSameAs(order);
    }

    @Test
    @DisplayName("Failed findById")
    public void test_failed_findById () {
        //given
        given(orderRepository.findById(order.getId())).willReturn(Optional.empty());

        // when
        Optional<Order> order1 = orderService.findById(order.getId());

        // then
        assertThat(order1).isEmpty();
    }

    @Test
    @DisplayName("Successful cartToOrder")
    public void test_successful_cartToOrder () {
        //given
        account.setCart(cart);
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(productService.findById(product.getId())).willReturn(Optional.of(product));
        given(orderRepository.save(Mockito.any(Order.class))).willAnswer(i -> i.getArgument(0, Order.class));
        Order order1 = Order.builder()
                .payment(cart.getPayment())
                .account(account)
                .build();
        order1.addProduct(product, 1);

        // when
        Order order2 = orderService.cartToOrder();
        order1.setAudit(order2.getAudit());

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(order2).isSameAs(captor.getValue()).isEqualTo(order1);
    }

    @Test
    @DisplayName("Failed cartToOrder - account not found")
    public void test_failed_cartToOrder_accountNotFound () {
        //given
        given(accountService.findByUsername(Mockito.anyString())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(ResponseStatusException.class, () ->
                orderService.cartToOrder());

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT NOT FOUND");
    }

    @Test
    @DisplayName("Failed cartToOrder - cart empty")
    public void test_failed_cartToOrder_cartEmpty () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));

        // when
        Exception exception = assertThrows(ResponseStatusException.class, () -> orderService.cartToOrder());

        // then
        assertThat(exception).hasMessageContaining("CART IS EMPTY");
    }

    @Test
    @DisplayName("Failed cartToOrder - product not found")
    public void test_failed_cartToOrder_productNotFound () {
        //given
        account.setCart(cart);
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(productService.findById(product.getId())).willReturn(Optional.empty());

        // when, then
        assertThrows(ProductNotFoundException.class, () -> orderService.cartToOrder());
    }

    @Test
    @DisplayName("Successful createOrder")
    public void test_successful_createOrder () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(productService.findById(product.getId())).willReturn(Optional.of(product));
        given(orderRepository.save(Mockito.any(Order.class))).willAnswer(i -> i.getArgument(0, Order.class));
        Order order1 = Order.builder()
                .account(account)
                .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.PAID))
                .build();
        order1.addProduct(product, 1);

        // when
        Order order2 = orderService.createOrder(cartDto, account.getUsername());
        order1.setAudit(order2.getAudit());

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(order2).isSameAs(captor.getValue()).isEqualTo(order1);
    }

    @Test
    @DisplayName("Failed createOrder - account not found")
    public void test_failed_createOrder_accountNotFound () {
        //given
        given(accountService.findByUsername(Mockito.anyString())).willReturn(Optional.empty());

        // when, then
        assertThrows(AccountNotFoundException.class, () ->
                orderService.createOrder(cartDto, account.getUsername()));
    }

    @Test
    @DisplayName("Failed createOrder - product not found")
    public void test_failed_createOrder_productNotFound () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(productService.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // when, then
        assertThrows(ProductNotFoundException.class, () ->
                orderService.createOrder(cartDto, account.getUsername()));
    }

    @Test
    @DisplayName("Successful deleteOrder")
    public void test_successful_deleteOrder () {
        //given
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        // when
        Order order1 = orderService.deleteOrder(order.getId());

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).delete(captor.capture());
        assertThat(order1).isSameAs(captor.getValue()).isEqualTo(order);
    }

    @Test
    @DisplayName("Failed deleteOrder")
    public void test_failed_deleteOrder () {
        // given
        given(orderRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(ResponseStatusException.class, () ->
                orderService.deleteOrder(101L));

        // then
        assertThat(exception).hasMessageContaining("ORDER NOT FOUND");
    }

    @Test
    @DisplayName("Successful findByLogin - username")
    public void test_successful_findByLogin_username () {
        //given
        Order order1 = order.toBuilder().id(101L).build();
        given(accountService.findByLogin(account.getUsername())).willReturn(Optional.of(account));
        given(orderRepository.findByAccountId(account.getId())).willReturn(List.of(order, order1));

        // when
        List<Order> orders = orderService.findByLogin(account.getUsername());

        // then
        assertThat(orders).hasSize(2).contains(order).contains(order1);
    }

    @Test
    @DisplayName("Successful findByLogin - email")
    public void test_successful_findByLogin_email () {
        //given
        Order order1 = order.toBuilder().id(101L).build();
        given(accountService.findByLogin(account.getEmail())).willReturn(Optional.of(account));
        given(orderRepository.findByAccountId(account.getId())).willReturn(List.of(order, order1));

        // when
        List<Order> orders = orderService.findByLogin(account.getEmail());

        // then
        assertThat(orders).hasSize(2).contains(order).contains(order1);
    }

    @Test
    @DisplayName("Failed findByLogin")
    public void test_failed_findByLogin () {
        //given
        given(accountService.findByLogin(Mockito.anyString())).willReturn(Optional.empty());

        // when, then
        assertThrows(AccountNotFoundException.class, () -> orderService.findByLogin("badLogin"));
    }

}
