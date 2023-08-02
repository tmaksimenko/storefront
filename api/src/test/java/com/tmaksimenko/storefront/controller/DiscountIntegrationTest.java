package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.discount.DiscountCreateDto;
import com.tmaksimenko.storefront.dto.discount.DiscountDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.discount.Discount;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tmaksimenko.storefront.enums.DiscountType.PRODUCT;
import static com.tmaksimenko.storefront.enums.DiscountType.ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DiscountIntegrationTest {

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
    DiscountService discountService;

    @Autowired
    ProductService productService;

    GeneralDiscount generalDiscount;

    ProductDiscount productDiscount;

    Product product;

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
        AccountFullDto adminFullDto = adminDto.toFullDto(Role.ROLE_ADMIN);
        adminFullDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        accountService.createAccount(adminFullDto);

        product = productService.createProduct(
                ProductCreateDto.builder()
                        .name("name")
                        .brand("brand")
                        .price(10.0)
                        .weight(1.0).build());

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
        @SuppressWarnings("unchecked")
        List<? extends Discount> discounts = (List<? extends Discount>) discountService.findAll();
        for (Discount discount : discounts)
            discountService.deleteDiscount(discount.getId());

        generalDiscount = GeneralDiscount.builder()
                .id(1L)
                .percent(10.0)
                .role(Role.ROLE_STAFF)
                .build();

        productDiscount = ProductDiscount.builder()
                .id(2L)
                .percent(10.0)
                .build();

        productDiscount.setProduct(productService.findAll().get(0));

        discountService.createDiscount(generalDiscount);
        discountService.createDiscount(productDiscount);
    }


    private String status (int code) {
        return String.format("\"status\":%s", code);
    }

    private String error (String message) {
        return String.format("\"error\":\"%s\"", message);
    }

    @Test
    @DisplayName("Successful admin viewAll")
    public void test_successful_viewAll () {
        // when
        @SuppressWarnings("rawtypes") // intentional raw use of parameterized class
        List result = this.restTemplate.exchange(baseURL + "/admin/discounts/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).extracting("percent", "role")
                .containsExactly(generalDiscount.getPercent(), generalDiscount.getRole().name());
        assertThat(result.get(1)).extracting("percent")
                    .isEqualTo(productDiscount.getPercent());
        assertThat(result.get(1)).extracting("product")
                    .extracting("price").isEqualTo(10.0);
    }

    @Test
    @DisplayName("Successful admin view - general")
    public void test_successful_admin_viewGeneral () {
        // given
        Long id = discountService.findByRole(Role.ROLE_STAFF).get(0).getId();

        // when
        DiscountDto discount = this.restTemplate.exchange(baseURL + "/admin/discounts/view?id=" + id,
                HttpMethod.GET, new HttpEntity<>(headers), DiscountDto.class).getBody();

        // then
        assertThat(discount).isNotNull().extracting("type").isEqualTo(ROLE);
        assertThat(discount).extracting("percent", "role")
                .containsExactly(generalDiscount.getPercent(), generalDiscount.getRole().name());
    }

    @Test
    @DisplayName("Successful admin view - product")
    public void test_successful_admin_viewProduct () {
        // given
        Long id = discountService.findByProductId(product.getId()).orElseThrow().getId();

        // when
        DiscountDto discount = this.restTemplate.exchange(baseURL + "/admin/discounts/view?id=" + id,
                HttpMethod.GET, new HttpEntity<>(headers), DiscountDto.class).getBody();

        // then
        assertThat(discount).isNotNull().extracting("type").isEqualTo(PRODUCT);
        assertThat(discount).extracting("percent", "product")
                .containsExactly(productDiscount.getPercent(), productDiscount.getProduct().toDto());
    }

    @Test
    @DisplayName("Failed admin view")
    public void test_failed_admin_view () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/discounts/view?id=" + badId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful admin add - general")
    public void test_successful_admin_addGeneral () {
        // given
        DiscountCreateDto discountCreateDto = DiscountCreateDto.builder()
                .percent(15.0)
                .role(Role.ROLE_ADMIN.name()).build();

        // when
        Discount newDiscount = this.restTemplate.exchange(baseURL + "/admin/discounts/add", HttpMethod.POST,
                new HttpEntity<>(discountCreateDto, headers), Discount.class).getBody();

        // then
        assertThat(newDiscount).isNotNull();
        List<DiscountDto> discounts = discountService.findAll().stream().map(x ->{
            if (x instanceof GeneralDiscount)
                return ((GeneralDiscount) x).toDto();
            else if (x instanceof ProductDiscount)
                return ((ProductDiscount) x).toDto();
            else
                throw new RuntimeException();
        }).toList();

        assertThat(discounts).hasSize(3);
        DiscountDto discount = discounts.get(1); // since generals are done first and products are added after in service

        assertThat(discount).isNotNull().extracting("type").isEqualTo(ROLE);
        assertThat(discount).extracting("percent", "role")
                .containsExactly(discountCreateDto.getPercent(), discountCreateDto.getRole());
    }

    @Test
    @DisplayName("Successful admin add - product")
    public void test_successful_admin_addProduct () {
        // given
        Product product1 = productService.createProduct(
                ProductCreateDto.builder()
                        .name("differentName")
                        .brand("differentBrand")
                        .price(12.0)
                        .weight(2.0).build());

        DiscountCreateDto discountCreateDto = DiscountCreateDto.builder()
                .percent(15.0)
                .productId(product1.getId()).build();

        // when
        Discount newDiscount = this.restTemplate.exchange(baseURL + "/admin/discounts/add", HttpMethod.POST,
                new HttpEntity<>(discountCreateDto, headers), Discount.class).getBody();

        // then
        assertThat(newDiscount).isNotNull();
        List<DiscountDto> discounts = discountService.findAll().stream().map(x ->{
            if (x instanceof GeneralDiscount)
                return ((GeneralDiscount) x).toDto();
            else if (x instanceof ProductDiscount)
                return ((ProductDiscount) x).toDto();
            else
                throw new RuntimeException();
        }).toList();

        assertThat(discounts).hasSize(3);
        DiscountDto discount = discounts.get(2);

        assertThat(discount).isNotNull().extracting("type").isEqualTo(PRODUCT);
        assertThat(discount).extracting("percent", "product")
                .containsExactly(discountCreateDto.getPercent(), product1.toDto());
    }

    @Test
    @DisplayName("Failed admin add - product discount already exists")
    public void test_failed_admin_addProduct_alreadyExists () {
        // when
        DiscountCreateDto discountCreateDto = DiscountCreateDto.builder()
                .percent(10.0)
                .productId(product.getId()).build();

        Exception response = assertThrows(HttpServerErrorException.class,()->this.restTemplate.exchange(baseURL + "/admin/discounts/add", HttpMethod.POST,
                new HttpEntity<>(discountCreateDto, headers), String.class));

        // then
        assertThat(response)
                .hasMessageContaining(status(500))
                .hasMessageContaining(error("Internal Server Error"));
    }

    @Test
    @DisplayName("Failed admin add - product not found")
    public void test_failed_admin_addProduct_notFound () {
        // when
        long badId = -1L;
        DiscountCreateDto discountCreateDto = DiscountCreateDto.builder()
                .percent(10.0)
                .productId(badId).build();

        String response = this.restTemplate.exchange(baseURL + "/admin/discounts/add", HttpMethod.POST,
                new HttpEntity<>(discountCreateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Successful admin delete - general")
    public void test_successful_admin_deleteGeneral () {
        // given
        long id = discountService.findByRole(generalDiscount.getRole()).get(0).getId();

        // when

        GeneralDiscount deletedDiscount = this.restTemplate.exchange(baseURL + "/admin/discounts/delete?id=" + id,
                HttpMethod.DELETE, new HttpEntity<>(headers), GeneralDiscount.class).getBody();

        // then
        List<? super Discount> discounts = discountService.findAll();
        assertThat(discounts).hasSize(1);
        assertThat(deletedDiscount).isNotNull();
        assertThat(deletedDiscount.toDto()).isEqualTo(generalDiscount.toDto());
    }

    @Test
    @DisplayName("Successful admin delete - product")
    public void test_successful_admin_deleteProduct () {
        // given
        long id = discountService.findByProductId(product.getId()).orElseThrow().getId();

        // when

        ProductDiscount deletedDiscount = this.restTemplate.exchange(baseURL + "/admin/discounts/delete?id=" + id,
                HttpMethod.DELETE, new HttpEntity<>(headers), ProductDiscount.class).getBody();

        // then
        List<? super Discount> discounts = discountService.findAll();
        assertThat(discounts).hasSize(1);
        assertThat(deletedDiscount).isNotNull();
        assertThat(deletedDiscount.toDto()).isEqualTo(productDiscount.toDto());
    }

    @Test
    @DisplayName("Failed admin delete")
    public void test_failed_admin_delete () {
        // given
        long badId = -1L;

        // when
        String response = this.restTemplate.exchange(baseURL + "/admin/discounts/delete?id=" + badId,
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
