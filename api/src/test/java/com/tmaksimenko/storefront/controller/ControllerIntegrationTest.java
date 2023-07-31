package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.service.account.AccountService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerIntegrationTest {

    @Value(value = "${local.server.port}")
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    AccountDto accountDto;

    AccountDto adminDto;

    AccountFullDto adminFullDto;

    @BeforeAll
    public void setupAll () {
        adminDto = AccountDto.builder()
                .username("testAdmin")
                .password("password")
                .email("adminMail@mail.com")
                .build();
        adminFullDto = adminDto.toFullDto(Role.ROLE_ADMIN);
        adminFullDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        accountService.createAccount(adminFullDto);

        accountDto = AccountDto.builder().username("testUser")
                .password("password")
                .email("mail@mail.com").build();

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is5xxServerError();
            }
        });
    }

    private String status (int code) {
        return String.format("\"status\":%s", code);
    }

    private String error (String message) {
        return String.format("\"error\":\"%s\"", message);
    }

    @Test
    @DisplayName("Successful authentication")
    public void test_successful_authentication () throws JSONException {
        // given
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", adminDto.getPassword());

        // when
        JSONObject response = new JSONObject(this.restTemplate.postForObject("http://localhost:" + port + "/auth",
                authRequestMap , String.class));

        // then
        assertThat(response.getString("login")).isEqualTo(adminDto.getUsername());
        assertThat(response.getString("token")).isNotNull();
        assertTrue(response.getString("token").length() > 100);
    }

    @Test
    @DisplayName("Failed authentication (not found)")
    public void test_failed_authentication_notFound () {
        // given
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", "badUserName");
        authRequestMap.put("password", "password");

        // when
        String response = this.restTemplate.postForObject("http://localhost:" + port + "/auth",
                authRequestMap , String.class);

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Failed authentication (bad password)")
    public void test_failed_authentication_badPassword () throws ResourceAccessException {
        // given
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", "badPassword");

        // when
        String response = this.restTemplate.postForObject("http://localhost:" + port + "/auth",
                authRequestMap , String.class);

        // then
        assertThat(response).contains(status(401)).contains(error("Unauthorized"));
    }


    @Test
    @DisplayName("Successful registration, authentication and get accounts/all")
    public void test_successful_register_andAuthenticateAsAdmin_andGetAllAccounts () throws JSONException {
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", adminDto.getPassword());

        assertThat(this.restTemplate.postForObject("http://localhost:" + port + "/register", accountDto, Account.class))
                .isInstanceOf(Account.class)
                .extracting("username", "email", "role")
                        .doesNotContainNull()
                        .containsExactly(accountDto.getUsername(), accountDto.getEmail(), Role.ROLE_USER);

        String tokenValue = new JSONObject(
                this.restTemplate.postForObject("http://localhost:" + port + "/auth",
                        authRequestMap, String.class))
                .getString("token");

        assertThat(tokenValue).isNotNull();
        assertTrue(tokenValue.length() > 100);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", tokenValue);

        @SuppressWarnings("all") // intentional raw use of parameterized class
        List result = this.restTemplate.exchange(
                "http://localhost:" + port + "/admin/accounts/all", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getBody();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).extracting("username", "email", "role")
                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(), adminFullDto.getRole().name());
        assertThat(result.get(1)).extracting("username", "email", "role")
                .containsExactly(accountDto.getUsername(), accountDto.getEmail(), Role.ROLE_USER.name());
    }




}
