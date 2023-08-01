package com.tmaksimenko.storefront.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmaksimenko.storefront.auth.JwtUtils;
import com.tmaksimenko.storefront.controller.user.ProductController;
import com.tmaksimenko.storefront.dto.product.ProductDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(ProductController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest {

    MockMvc mockMvc;

    @SuppressWarnings("unused") // required to exist in context
    @MockBean
    AccountService accountService;

    @MockBean
    ProductService productService;

    Product product;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @SuppressWarnings("unused")
    @MockBean
    BCryptPasswordEncoder passwordEncoder;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    UserDetailsService userDetailsService;

    final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public void setupAll () {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        passwordEncoder = new BCryptPasswordEncoder();
        given(jwtUtils.getUsernameFromToken(Mockito.anyString())).willReturn("testUser");
        given(jwtUtils.validateToken(Mockito.any(), Mockito.any())).willReturn(true);
        given(userDetailsService.loadUserByUsername("testUser")).willReturn(
                new org.springframework.security.core.userdetails.User(
                        "testUser",
                        "password",
                        AuthorityUtils.createAuthorityList(Role.ROLE_USER.name())));
    }


    @BeforeEach
    public void setup() {
        product = Product.builder()
                .id(1000L)
                .name("testName")
                .brand("testBrand")
                .price(10.0)
                .weight(1.0).build();
    }

    @Test
    @DisplayName("Successful findAll")
    public void test_findAll () throws Exception {
        // given
        Product product1 = product.toBuilder()
                .id(1001L)
                .name("testName1")
                .price(15.0)
                .build();
        given(productService.findAll()).willReturn(List.of(product, product1));

        // when
        String response = this.mockMvc.perform(get("/products/all"))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // then
        List<ProductDto> productList = objectMapper.readValue(response, new TypeReference<List<Product>>() {})
                .stream().map(Product::toDto).toList();
        assertThat(productList).hasSize(2).contains(product.toDto(), product1.toDto());
    }

    @Test
    @DisplayName("Successful findById")
    public void test_successful_findById () throws Exception {
        // given
        given(productService.findById(Mockito.anyLong())).willReturn(Optional.of(product));

        // when
        String response = this.mockMvc.perform(
                get("/products/view")
                        .param("id", String.valueOf(product.getId())))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // then
        assertEquals(product, objectMapper.readValue(response, Product.class));
    }

    @Test
    @DisplayName("Failed findById")
    public void test_failed_findById () throws Exception {
        // given
        given(productService.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // when, then
        this.mockMvc.perform(
                get("/products/view")
                        .param("id", "1001"))
                .andDo(print()).andExpect(status().isNotFound());
    }


}
