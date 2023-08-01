package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.payment.ExpiryDate;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.order.OrderService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Product product;

    AccountFullDto adminFullDto;

    HttpHeaders headers;

    @BeforeAll
    public void setupAll () throws JSONException {
        baseURL = "http://localhost:" + port;
        AccountDto adminDto = AccountDto.builder()
                .username("testAdmin")
                .password("password")
                .email("adminMail@mail.com")
                .build();
        adminFullDto = adminDto.toFullDto(Role.ROLE_ADMIN);
        adminFullDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        accountService.createAccount(adminFullDto);

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
        List<Order> orders = orderService.findAll();
        for (Order order : orders)
            orderService.deleteOrder(order.getId());
        orderService.createOrder(cartDto, adminFullDto.getUsername());
    }

    private String status (int code) {
        return String.format("\"status\":%s", code);
    }

    private String error (String message) {
        return String.format("\"error\":\"%s\"", message);
    }

//    @Test
//    @DisplayName("Successful view own order")
//    public void test_successful_viewSelfOrder () {
//        // when
//        Order result = this.restTemplate.exchange(
//                baseURL + "/order/view", HttpMethod.GET,
//                new HttpEntity<>(headers), Order.class).getBody();
//
//        // then
//        assertThat(result).isInstanceOf(Order.class).extracting("username", "email", "role")
//                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(), adminFullDto.getRole());
//    }

//    @Test
//    @DisplayName("Successful update own order - core fields")
//    public void test_successful_updateSelfOrder_core () {
//        // given
//        OrderUpdateDto updatedAdmin = new OrderUpdateDto(
//                OrderDto.builder().email("newAdminMail@mail.com").build(),
//                new Address());
//
//        // when
//        Order response = this.restTemplate.exchange(baseURL + "/order/update", HttpMethod.PUT,
//                new HttpEntity<>(updatedAdmin, headers), Order.class).getBody();
//
//        // then
//        assertThat(response).isInstanceOf(Order.class).extracting("username", "email", "role")
//                .containsExactly(adminFullDto.getUsername(), updatedAdmin.getOrderDto().getEmail(),
//                        adminFullDto.getRole());
//    }
//
//    @Test
//    @DisplayName("Successful update own order - address")
//    public void test_successful_updateSelfOrder_address () {
//        // given
//        Address address = Address.builder()
//                .streetAddress("27 Mobile Dr")
//                .postalCode("M1M1M1")
//                .country("Canada")
//                .build();
//
//        OrderUpdateDto orderUpdateDto = new OrderUpdateDto(OrderDto.builder().build(), address);
//
//        // when
//        Order response = this.restTemplate.exchange(baseURL + "/order/update", HttpMethod.PUT,
//                new HttpEntity<>(orderUpdateDto, headers), Order.class).getBody();
//
//        // then
//        assertThat(response).isInstanceOf(Order.class)
//                .extracting("username", "email", "role", "address")
//                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(),
//                        adminFullDto.getRole(), address);
//    }
//
//    @Test
//    @DisplayName("Failed update own order")
//    public void test_failed_updateSelfOrder () {
//        // given
//        OrderUpdateDto orderUpdateDto = new OrderUpdateDto(OrderDto.builder().build(), new Address());
//
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/order/update", HttpMethod.PUT,
//                new HttpEntity<>(orderUpdateDto, headers), String.class).getBody();
//
//        // then
//        assertThat(response).contains(status(403)).contains(error("Forbidden"));
//    }
//
//    @Test
//    @DisplayName("Successful delete own order")
//    public void test_successful_deleteOrder () {
//        // when
//        Order deletedOrder = this.restTemplate.exchange(baseURL + "/order/delete", HttpMethod.DELETE,
//                new HttpEntity<>(headers), Order.class).getBody();
//
//        // then
//        List<Order> orders = orderService.findAll();
//        assertThat(orders).isEmpty();
//        assertThat(deletedOrder).isNotNull();
//        assertThat(deletedOrder.toDto().toFullDto(deletedOrder.getRole())).isEqualTo(adminFullDto);
//    }
//
//    @Test
//    @DisplayName("Successful admin viewAll")
//    public void test_successful_viewAll () {
//        // when
//        @SuppressWarnings("all") // intentional raw use of parameterized class
//        List result = this.restTemplate.exchange(baseURL + "/admin/orders/all", HttpMethod.GET,
//                new HttpEntity<>(headers), List.class).getBody();
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0)).extracting("username", "email", "role")
//                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(), adminFullDto.getRole().name());
//    }
//
//    @Test
//    @DisplayName("Successful admin view")
//    public void test_successful_admin_view () {
//        // when
//        OrderFullDto order = this.restTemplate.exchange(baseURL + "/admin/orders/view" + "?login=" + adminFullDto.getUsername(),
//                HttpMethod.GET, new HttpEntity<>(headers), OrderFullDto.class).getBody();
//
//        // then
//        assertThat(order).isNotNull().extracting("username", "email", "password", "role")
//                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(),
//                        adminFullDto.getPassword(), adminFullDto.getRole());
//    }
//
//    @Test
//    @DisplayName("Failed admin view")
//    public void test_failed_admin_view () {
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/admin/orders/view" + "?login=badLogin",
//                HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
//
//        // then
//        assertThat(response).contains(status(404)).contains(error("Not Found"));
//    }
//
//    @Test
//    @DisplayName("Successful admin add")
//    public void test_successful_admin_add () {
//        // given
//        OrderCreateDto orderCreateDto = OrderCreateDto.builder()
//                .username(orderDto.getUsername())
//                .email(orderDto.getEmail())
//                .password(orderDto.getPassword())
//                .role(Role.ROLE_USER).build();
//
//        // when
//        Order newOrder = this.restTemplate.exchange(baseURL + "/admin/orders/add", HttpMethod.POST,
//                new HttpEntity<>(orderCreateDto, headers), Order.class).getBody();
//
//        // then
//        assertThat(newOrder).isNotNull();
//        List<Order> orders = orderService.findAll();
//
//        assertThat(orders).hasSize(2);
//        assertThat(orders.get(1)).extracting("username", "email", "password", "role")
//                .containsExactly(newOrder.getUsername(), newOrder.getEmail(),
//                        newOrder.getPassword(), newOrder.getRole());
//        assertThat(orders.get(1).getAudit().getCreatedBy()).isEqualTo(adminFullDto.getUsername());
//    }
//
//    @Test
//    @DisplayName("Failed admin add - bad role")
//    public void test_failed_admin_add_badRole () {
//        // given
//        class BadRoleOrderDto extends OrderDto {
//            final String role;
//            BadRoleOrderDto (String role) {this.role = role;}
//        }
//        BadRoleOrderDto badRoleOrderDto = new BadRoleOrderDto("ROLE_BAD_ROLE");
//
//        badRoleOrderDto.setUsername(orderDto.getUsername());
//        badRoleOrderDto.setEmail(orderDto.getEmail());
//        badRoleOrderDto.setPassword(orderDto.getPassword());
//
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/admin/orders/add", HttpMethod.POST,
//                new HttpEntity<>(badRoleOrderDto, headers), String.class).getBody();
//        System.out.println(response);
//        // then
//        assertThat(response).contains(status(403)).contains(error("Forbidden"));
//    }
//
//    @Test
//    @DisplayName("Failed admin add - order already exists")
//    public void test_failed_admin_add_alreadyExists () {
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/admin/orders/add", HttpMethod.POST,
//                new HttpEntity<>(adminFullDto, headers), String.class).getBody();
//
//        // then
//        assertThat(response).contains(status(403)).contains(error("Forbidden"));
//    }
//
//    @Test
//    @DisplayName("Successful admin update")
//    public void test_successful_admin_update () {
//        // given
//        OrderFullDto orderFullDto = adminFullDto.toBuilder().password("newPassword").build();
//
//        // when
//        Order response = this.restTemplate.exchange(baseURL + "/admin/orders/update", HttpMethod.PUT,
//                new HttpEntity<>(orderFullDto, headers), Order.class).getBody();
//
//        // then
//        assertThat(response).isNotNull().isInstanceOf(Order.class);
//        assertThat(response.getPassword()).isNotEqualTo(adminFullDto.getPassword());
//    }
//
//    @Test
//    @DisplayName("Failed admin update")
//    public void test_failed_admin_update () {
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/admin/orders/update", HttpMethod.PUT,
//                new HttpEntity<>(headers), String.class).getBody();
//
//        // then
//        assertThat(response).contains(status(400)).contains(error("Bad Request"));
//    }
//
//    @Test
//    @DisplayName("Successful admin delete")
//    public void test_successful_admin_delete () {
//        // when
//        Order deletedOrder = this.restTemplate.exchange(baseURL + "/admin/orders/delete?login="
//                        + adminFullDto.getUsername(),
//                HttpMethod.DELETE, new HttpEntity<>(headers), Order.class).getBody();
//
//        // then
//        List<Order> orders = orderService.findAll();
//        assertThat(orders).isEmpty();
//        assertThat(deletedOrder).isNotNull();
//        assertThat(deletedOrder.toDto().toFullDto(deletedOrder.getRole())).isEqualTo(adminFullDto);
//    }
//
//    @Test
//    @DisplayName("Failed admin delete")
//    public void test_failed_admin_delete () {
//        // when
//        String response = this.restTemplate.exchange(baseURL + "/admin/orders/delete?login=badLogin",
//                HttpMethod.DELETE, new HttpEntity<>(headers), String.class).getBody();
//
//        // then
//        assertThat(response).contains(status(404)).contains(error("Not Found"));
//    }



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
