package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.controller.user.ProductController;
import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.payment.ExpiryDate;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.discount.DiscountService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductIntegrationTest {

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
    ProductService productService;

    @Autowired
    DiscountService discountService;

    @Autowired
    ProductController productController;

    HttpHeaders headers;

    Product product;

    Account admin;

    CartDto cartDto;

    @BeforeAll
    public void setupAll () throws JSONException {
        for (Account account : accountService.findAll())
            accountService.deleteAccount(account.getId());
        
        for (Object discount : discountService.findAll())
            if (discount.getClass().equals(ProductDiscount.class))
                discountService.deleteDiscount(((ProductDiscount) discount).getId());

        product = productService.createProduct(
                ProductCreateDto.builder()
                        .name("name")
                        .brand("brand")
                        .price(10.0)
                        .weight(1.0).build());

        baseURL = "http://localhost:" + port;
        AccountDto adminDto = AccountDto.builder()
                .username("testAdmin")
                .password("password")
                .email("adminMail@mail.com")
                .build();
        AccountFullDto adminFullDto = adminDto.toFullDto(Role.ROLE_ADMIN);
        adminFullDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        admin = accountService.createAccount(adminFullDto);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        admin.getUsername(),
                        admin.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

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

        accountService.addCart(productService.createCart(cartDto, admin.getUsername()));
        admin = accountService.findById(admin.getId()).orElseThrow(); // updates admin field to have cart

        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", adminDto.getPassword());
        headers = getTokenAsHeaders(authRequestMap);

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is5xxServerError();
            }
        });

    }

    @BeforeEach
    public void setup () {
        List<Product> products = productService.findAll();
        for (Product tproduct : products)
            productService.deleteProduct(tproduct.getId());

        product = productService.createProduct(ProductCreateDto.builder()
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice())
                .weight(product.getWeight()).build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        admin.getUsername(),
                        admin.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        CartItemDto cartItemDto = new CartItemDto(product.getId());
        cartDto.setCartItemDtos(List.of(cartItemDto));

        accountService.addCart(productService.createCart(cartDto, admin.getUsername()));
        admin = accountService.findById(admin.getId()).orElseThrow(); // updates admin field to have cart
    }

    private String status (int code) {
        return String.format("\"status\":%s", code);
    }

    private String error (String message) {
        return String.format("\"error\":\"%s\"", message);
    }

    @Test
    @DisplayName("Successful viewAll")
    public void test_successful_viewAll () {
        // when
        @SuppressWarnings("rawtypes")
        List products = this.restTemplate.getForObject(baseURL + "/products/all", List.class);

        // then
        assertThat(products).isNotEmpty();
        assertThat(products.get(0)).extracting("name", "brand", "price").containsExactly(
                product.getName(), product.getBrand(), product.getPrice());
    }

    @Test
    @DisplayName("Empty viewAll")
    public void test_empty_viewAll () {
        // given
        List<Product> productList = productService.findAll();
        for (Product tproduct : productList)
            productService.deleteProduct(tproduct.getId());
        productController.emptyCache();

        // when
        @SuppressWarnings("rawtypes")
        List products = this.restTemplate.getForObject(baseURL + "/products/all", List.class);

        // then
        assertThat(products).isEmpty();
    }


    @Test
    @DisplayName("Successful view product")
    public void test_successful_viewProduct () {
        // when
        Product product1 = this.restTemplate.exchange(baseURL + "/products/view?id=" + product.getId(),
                HttpMethod.GET, new HttpEntity<>(headers), Product.class).getBody();

        // then
        assertThat(product1).isEqualTo(product);
    }

    @Test
    @DisplayName("Failed view product")
    public void test_failed_viewProduct () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/products/view?id=" + badId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error(("Not Found")));
    }

    @Test
    @DisplayName("Successful view Cart")
    public void test_successful_viewCart () {
        // when
        Cart cart = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.GET,
                new HttpEntity<>(headers), Cart.class).getBody();

        // then
        Cart cart1 = admin.getCart();
        cart1.getPayment().setPaymentInfo(null);
        assertThat(cart).isEqualTo(cart1);
    }

    @Test
    @DisplayName("Empty view Cart")
    public void test_empty_viewCart () {
        // given
        accountService.addCart(null);

        // when
        Cart cart = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.GET,
                new HttpEntity<>(headers), Cart.class).getBody();

        // then
        assertThat(cart).isEqualTo(Cart.builder()
                .payment(null)
                .price(null)
                .items(new HashMap<>()).build());
    }

    @Test
    @DisplayName("Successful delete Cart")
    public void test_successful_deleteCart () {
        // when
        String response = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains("CART DELETED");
    }

    @Test
    @DisplayName("Empty delete Cart")
    public void test_empty_deleteCart () {
        // given
        accountService.addCart(null);

        // when
        String response = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains("NO CART FOUND");
    }

    @Test
    @DisplayName("Successful create Cart")
    public void test_successful_createCart () {
        // given
        accountService.addCart(null);

        String response = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.POST,
                new HttpEntity<>(cartDto, headers), String.class).getBody();

        // then
        assertThat(response).contains("CART CREATED");
    }

    @Test
    @DisplayName("Successful create Cart - replaced")
    public void test_successful_replaceCart () {
        // when
        String response = this.restTemplate.exchange(baseURL + "/products/cart", HttpMethod.POST,
                new HttpEntity<>(cartDto, headers), String.class).getBody();

        // then
        assertThat(response).contains("CART CREATED");
    }

    @Test
    @DisplayName("Successful create Product")
    public void test_successful_adminCreateProduct () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder()
                .name("someName")
                .brand("someBrand")
                .price(15.0)
                .weight(2.0).build();

        // when
        Product response = this.restTemplate.exchange(baseURL + "/admin/products/add", HttpMethod.POST,
                new HttpEntity<>(productCreateDto, headers), Product.class).getBody();

        // then
        assertThat(response).extracting("name", "brand", "price", "weight").containsExactly(
                productCreateDto.getName(), productCreateDto.getBrand(), productCreateDto.getPrice(),
                productCreateDto.getWeight());
    }

    @Test
    @DisplayName("Failed create Product - name")
    public void test_failed_adminCreateProduct_noName () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder().build();

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/add", HttpMethod.POST,
                new HttpEntity<>(productCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Failed create Product - brand")
    public void test_failed_adminCreateProduct_noBrand () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder().name("someName").build();

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/add", HttpMethod.POST,
                new HttpEntity<>(productCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Failed create Product - price")
    public void test_failed_adminCreateProduct_noPrice () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder()
                .name("someName")
                .brand("someBrand").build();

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/add", HttpMethod.POST,
                new HttpEntity<>(productCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Failed create Product - weight")
    public void test_failed_adminCreateProduct_noWeight () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder()
                .name("someName")
                .brand("someBrand")
                .price(10.0).build();

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/add", HttpMethod.POST,
                new HttpEntity<>(productCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Successful update Product - all")
    public void test_successful_adminUpdateProduct_all () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder()
                .name("someName")
                .brand("someBrand")
                .price(15.0)
                .weight(2.0).build();

        // when
        Product updatedProduct = this.restTemplate.exchange(baseURL + "/admin/products/update?id=" + product.getId(),
                HttpMethod.PUT, new HttpEntity<>(productCreateDto, headers), Product.class).getBody();

        // then
        assertThat(updatedProduct).extracting("name", "brand", "price", "weight").containsExactly(
                productCreateDto.getName(), productCreateDto.getBrand(), productCreateDto.getPrice(),
                productCreateDto.getWeight());
    }

    @Test
    @DisplayName("Successful update Product - none")
    public void test_successful_adminUpdateProduct_none () {
        // given
        ProductCreateDto productCreateDto = ProductCreateDto.builder().build();

        // when
        Product unchangedProduct = this.restTemplate.exchange(baseURL + "/admin/products/update?id=" + product.getId(),
                HttpMethod.PUT, new HttpEntity<>(productCreateDto, headers), Product.class).getBody();

        // then
        assertThat(unchangedProduct).extracting("name", "brand", "price", "weight").containsExactly(
                product.getName(), product.getBrand(), product.getPrice(), product.getWeight());
    }

    @Test
    @DisplayName("Failed update product - product not found")
    public void test_failed_adminUpdateProduct_notFound () {
        // given
        long badId = -1L;
        ProductCreateDto productCreateDto = ProductCreateDto.builder().build();

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/update?id=" + badId,
                HttpMethod.PUT, new HttpEntity<>(productCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful delete Product")
    public void test_successful_adminDeleteProduct () {
        // when
        Product deletedProduct = this.restTemplate.exchange(baseURL + "/admin/products/delete?id=" + product.getId(),
                HttpMethod.DELETE, new HttpEntity<>(headers), Product.class).getBody();

        // then
        assertThat(deletedProduct).extracting("name", "brand", "price", "weight").containsExactly(
                product.getName(), product.getBrand(), product.getPrice(), product.getWeight());
    }

    @Test
    @DisplayName("Failed delete Product")
    public void test_failed_adminDeleteProduct () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/products/delete?id=" + badId,
                HttpMethod.DELETE, new HttpEntity<>(headers), String.class).getBody();

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
