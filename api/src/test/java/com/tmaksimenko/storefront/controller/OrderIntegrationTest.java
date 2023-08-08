package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.order.OrderGetDto;
import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.payment.ExpiryDate;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderIntegrationTest {

    @Value(value = "${local.server.port}")
    int port;
    
    String baseURL;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    @Autowired
    OrderService orderService;

    @Autowired
    ProductService productService;

    Order order;

    CartDto cartDto;

    Product product, product1;

    AccountFullDto adminFullDto, testAccountDto;

    HttpHeaders headers;

    @BeforeAll
    public void setupAll () throws JSONException {
        for (Account account : accountService.findAll())
            accountService.deleteAccount(account.getId());

        baseURL = "http://localhost:" + port;
        AccountDto adminDto = AccountDto.builder()
                .username("testAdmin")
                .password("password")
                .email("adminMail@mail.com")
                .build();
        adminFullDto = adminDto.toFullDto(Role.ROLE_ADMIN);
        adminFullDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        accountService.createAccount(adminFullDto);

        testAccountDto = AccountFullDto.builder()
                .username("testUser")
                .email("testMail@mail.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .build();

        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", adminDto.getPassword());
        headers = getTokenAsHeaders(authRequestMap);

        product = productService.createProduct(
                ProductCreateDto.builder()
                        .name("name")
                        .brand("brand")
                        .price(10.0)
                        .weight(1.0).build());

        product1 = productService.createProduct(
                ProductCreateDto.builder()
                        .name("differentName")
                        .brand("differentBrand")
                        .price(15.0)
                        .weight(2.0).build());

        cartDto = CartDto.builder()
                .paymentCreateDto(PaymentCreateDto.builder()
                        .paymentInfo(PaymentInfo.builder()
                                .cardNumber(1111111111111111L)
                                .expiry(new ExpiryDate(8, 23))
                                .securityCode(111)
                                .postalCode("M1M1M1")
                                .build())
                        .paymentProvider(PaymentProvider.VISA)
                        .build())
                .build();
        CartItemDto cartItemDto = new CartItemDto(product.getId());
        cartDto.setCartItemDtos(List.of(cartItemDto));

        order = orderService.createOrder(cartDto, adminFullDto.getUsername());

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is5xxServerError();
            }
        });
    }

    @BeforeEach
    public void setup () {
        List<OrderGetDto> orders = orderService.findAll();
        for (OrderGetDto order : orders)
            orderService.deleteOrder(order.getId());
        List<Account> accounts = accountService.findAll();
        for (Account account : accounts)
            accountService.deleteAccount(account.getId());

        accountService.createAccount(adminFullDto);
        order = orderService.createOrder(cartDto, adminFullDto.getUsername());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminFullDto.getUsername(),
                        adminFullDto.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
    }

    private String status (int code) {
        return String.format("\"status\":%s", code);
    }

    private String error (String message) {
        return String.format("\"error\":\"%s\"", message);
    }

    @Test
    @DisplayName("Successful self viewAll")
    public void test_successful_viewAll () {
        // when
        @SuppressWarnings("rawtypes")
        List result = this.restTemplate.exchange(baseURL + "/orders/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).extracting("username").isEqualTo(adminFullDto.getUsername());
        assertThat(result.get(0)).extracting("paymentGetDto").extracting("paymentStatus")
                .isEqualTo(PaymentStatus.PAID.name());
    }

    @Test
    @DisplayName("Failed self viewAll")
    public void test_failed_viewAll () {
        // given
        orderService.deleteOrder(orderService.findAll().get(0).getId());

        // when
        @SuppressWarnings("rawtypes")
        List result = this.restTemplate.exchange(baseURL + "/orders/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        // then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Successful view own order")
    public void test_successful_viewSelfOrder () {
        // given
        long id = orderService.findAll().get(0).getId();

        // when
        OrderGetDto result = this.restTemplate.exchange(
                baseURL + "/orders/view?id=" + id, HttpMethod.GET,
                new HttpEntity<>(headers), OrderGetDto.class).getBody();

        // then
        assertThat(result).extracting("username").isEqualTo(adminFullDto.getUsername());
        assertThat(result).extracting("paymentGetDto").extracting("paymentStatus")
                .isEqualTo(PaymentStatus.PAID);
    }
    @Test
    @DisplayName("Successful view own order")
    public void test_failed_viewSelfOrder () {
        // given
        long badId = -1L;

        // when
        String result = this.restTemplate.exchange(
                baseURL + "/orders/view?id=" + badId, HttpMethod.GET,
                new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(result).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful submit cart")
    public void test_successful_submit () {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminFullDto.getUsername(),
                        adminFullDto.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                )
        );
        List<CartItemDto> cartItemDtos = List.of(new CartItemDto (product.getId(), 2));
        CartDto cartDto1 = CartDto.builder()
                .paymentCreateDto(cartDto.getPaymentCreateDto())
                .cartItemDtos(cartItemDtos).build();

        accountService.addCart(productService.createCart(cartDto1, adminFullDto.getUsername()));

        // when
        OrderGetDto placedOrder = this.restTemplate.exchange(baseURL + "/orders/submit", HttpMethod.POST,
                new HttpEntity<>(headers), OrderGetDto.class).getBody();

        // then
        assertThat(placedOrder).isNotNull();
        assertThat(orderService.findAll()).hasSize(2);
        assertThat(placedOrder).extracting("username", "paymentGetDto")
                .containsExactly(order.toFullDto().getUsername(), order.toFullDto().getPaymentGetDto());
        assertThat(placedOrder.getItems().get(0)).isNotNull().extracting("quantity").isEqualTo(2);
    }
    @Test
    @DisplayName("Failed submit cart")
    public void test_failed_submit () {
        System.out.println(adminFullDto);
        System.out.println(accountService.findAll());
        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/submit", HttpMethod.POST,
                new HttpEntity<>(headers), String.class).getBody();

        System.out.println(response);
        System.out.println(accountService.findAll());
        // then
        assertThat(orderService.findAll()).hasSize(1);
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful updateOrder")
    public void test_successful_updateOrder_all () {
        // given
        long badId = -1L;

        Map<Long, Integer> body = new HashMap<>();
        body.put(product.getId(), 2);
        body.put(product1.getId(), 1);
        body.put(badId, 1);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/update?id="+order.getId(), HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();
        System.out.println(response);

        // then
        assertThat(response)
                .contains("UPDATED PRODUCTS -> [" + product.getId())
                .contains("ADDED PRODUCTS -> [" + product1.getId())
                .contains("NOT FOUND -> [" + badId);
    }

    @Test
    @DisplayName("Successful updateOrder - not updated")
    public void test_successful_updateOrder_unUpdated () {
        // given
        Map<Long, Integer> body = new HashMap<>();
        body.put(product.getId(), 1);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/update?id="+order.getId(), HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();
        System.out.println(response);

        // then
        assertThat(response)
                .contains("UPDATED PRODUCTS -> []")
                .contains("ADDED PRODUCTS -> []")
                .contains("NOT FOUND -> []");
    }

    @Test
    @DisplayName("Failed updateOrder - not order owner")
    public void test_failed_updateOrder_notOwner () throws JSONException {
        // given
        accountService.createAccount(testAccountDto);

        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", testAccountDto.getUsername());
        authRequestMap.put("password", "password");
        HttpHeaders headers1 = getTokenAsHeaders(authRequestMap);

        long badId = -1L;
        Map<Long, Integer> body = new HashMap<>();
        body.put(product.getId(), 2);
        body.put(product1.getId(), 1);
        body.put(badId, 1);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/update?id="+order.getId(), HttpMethod.PUT,
                new HttpEntity<>(body, headers1), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Failed updateOrder - not found")
    public void test_failed_updateOrder_notFound () {
        // given
        long badId = -1L;

        Map<Long, Integer> body = Collections.singletonMap(product.getId(), 2);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/update?id="+badId, HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404), error("Not Found"));
    }

    @Test
    @DisplayName("Failed updateOrder - not owned by request maker")
    public void test_failed_updateOrder_notOwned () {
        // given
        Account account = accountService.createAccount(testAccountDto);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        account.getUsername(),
                        adminFullDto.getPassword()));

        accountService.addCart(Cart.builder()
                .items(Collections.singletonMap(product1.getId(), 1))
                .price(15.2)
                .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID)).build());

        orderService.cartToOrder();
        long orderId = orderService.findByLogin(account.getUsername()).get(0).getId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminFullDto.getUsername(),
                        adminFullDto.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        Map<Long, Integer> body = Collections.singletonMap(product.getId(), 2);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/update?id="+orderId, HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();
        System.out.println(response);
        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Successful delete own order")
    public void test_successful_deleteOrder () {
        // when
        OrderGetDto deletedOrder = this.restTemplate.exchange(baseURL + "/orders/delete?id=" + order.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), OrderGetDto.class).getBody();

        // then
        List<OrderGetDto> orders = orderService.findAll();
        assertTrue(orders.stream().map(OrderGetDto::getId).noneMatch(x -> Objects.equals(x, order.getId())));
        assertThat(deletedOrder).isNotNull();
        assertThat(deletedOrder.getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("Failed delete own order - not found")
    public void test_failed_deleteOrder_notFound () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/delete?id=" + badId, HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Failed delete own order - not order owner")
    public void test_failed_deleteOrder_notOwner () throws JSONException {
        // given
        Account account = accountService.createAccount(AccountFullDto.builder()
                .username("testUser")
                .password(passwordEncoder.encode("password"))
                .email("testMail@mail.com")
                .role(Role.ROLE_USER).build());

        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", account.getUsername());
        authRequestMap.put("password", "password");
        HttpHeaders headers1 = getTokenAsHeaders(authRequestMap);

        // when
        String response = this.restTemplate.exchange(baseURL + "/orders/delete?id=" + order.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers1), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Successful admin viewAll")
    public void test_successful_adminViewAll () {
        // when
        @SuppressWarnings("rawtypes") // intentional raw use of parameterized class
        List result = this.restTemplate.exchange(baseURL + "/admin/orders/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).extracting("username", "id")
                .containsExactly(adminFullDto.getUsername(), order.getId().intValue());
    }

    @Test
    @DisplayName("Empty admin viewAll")
    public void test_empty_adminViewAll () {
        // given
        orderService.deleteOrder(order.getId());

        // when
        @SuppressWarnings("rawtypes") // intentional raw use of parameterized class
        List result = this.restTemplate.exchange(baseURL + "/admin/orders/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Successful admin view")
    public void test_successful_admin_view () {
        // when
        OrderGetDto orderGetDto = this.restTemplate.exchange(baseURL + "/admin/orders/view" + "?id=" + order.getId(),
                HttpMethod.GET, new HttpEntity<>(headers), OrderGetDto.class).getBody();

        // then
        assertThat(orderGetDto).isNotNull().extracting("username", "id")
                .containsExactly(adminFullDto.getUsername(), order.getId());
    }

    @Test
    @DisplayName("Failed admin view")
    public void test_failed_admin_view () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/orders/view" + "?id=" + badId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful createOrder")
    public void test_successful_adminCreateOrder () {
        // given
        CartDto cartDto1 = cartDto.toBuilder().paymentCreateDto(
                cartDto.getPaymentCreateDto().toBuilder()
                    .paymentProvider(PaymentProvider.MASTERCARD).build())
                .build();

        // when
        OrderGetDto orderGetDto = this.restTemplate.exchange(baseURL + "/admin/orders/create?username=" + adminFullDto.getUsername(),
                HttpMethod.POST, new HttpEntity<>(cartDto1, headers), OrderGetDto.class).getBody();

        // then
        assertThat(orderGetDto).extracting("id").isNotEqualTo(order.getId());
        assertThat(orderGetDto).extracting("username").isEqualTo(adminFullDto.getUsername());
    }






    @Test
    @DisplayName("Successful admin updateOrder")
    public void test_successful_adminUpdateOrder_all () {
        // given
        long badId = -1L;

        Map<Long, Integer> body = new HashMap<>();
        body.put(product.getId(), 2);
        body.put(product1.getId(), 1);
        body.put(badId, 1);

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/orders/update?id="+order.getId(), HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();
        System.out.println(response);

    }

    @Test
    @DisplayName("Failed admin updateOrder - not found")
    public void test_failed_adminUpdateOrder_notFound () {
        // given
        long badId = -1L;

        Map<Long, Integer> body = Collections.singletonMap(product.getId(), 2);

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/orders/update?id="+badId, HttpMethod.PUT,
                new HttpEntity<>(body, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404), error("Not Found"));
    }

    @Test
    @DisplayName("Successful delete order")
    public void test_successful_adminDeleteOrder () {
        // when
        OrderGetDto deletedOrder = this.restTemplate.exchange(baseURL + "/admin/orders/delete?id=" + order.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), OrderGetDto.class).getBody();

        // then
        List<OrderGetDto> orders = orderService.findAll();
        assertTrue(orders.stream().map(OrderGetDto::getId).noneMatch(x -> Objects.equals(x, order.getId())));
        assertThat(deletedOrder).isNotNull();
        assertThat(deletedOrder.getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("Failed delete order - not found")
    public void test_failed_adminDeleteOrder_notFound () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/orders/delete?id=" + badId, HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    private HttpHeaders getTokenAsHeaders(Map<String, String> authRequestMap) throws JSONException {
        String tokenValue = new JSONObject(
                this.restTemplate.postForObject(baseURL + "/auth",
                        authRequestMap, String.class))
                .getString("token");

        assertThat(tokenValue).isNotNull();
        assertTrue(tokenValue.length() > 100);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", tokenValue);
        return headers;
    }

}
