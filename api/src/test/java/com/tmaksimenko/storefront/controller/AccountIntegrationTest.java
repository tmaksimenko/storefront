package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.dto.account.AccountUpdateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.service.account.AccountService;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTest {

    @Value(value = "${local.server.port}")
    int port;
    
    String baseURL;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;

    AccountDto accountDto;

    AccountDto adminDto;

    AccountFullDto adminFullDto;

    Map<String, String> authRequestMap;

    @BeforeAll
    public void setupAll () {
        baseURL = "http://localhost:" + port;
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

        authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", adminDto.getPassword());

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is5xxServerError();
            }
        });
    }

    @BeforeEach
    public void setup () {
        List<Account> accounts = accountService.findAll();
        for (Account account : accounts)
            accountService.deleteAccount(account.getId());
        accountService.createAccount(adminFullDto);
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
        JSONObject response = new JSONObject(this.restTemplate.postForObject(baseURL + "/auth",
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
        String response = this.restTemplate.postForObject(baseURL + "/auth",
                authRequestMap , String.class);

        // then
        assertThat(response).contains(status(404)).contains(error("Not Found"));
    }

    @Test
    @DisplayName("Failed authentication (bad password)")
    public void test_failed_authentication_badPassword () {
        // given
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put("login", adminDto.getUsername());
        authRequestMap.put("password", "badPassword");

        // when
        String response = this.restTemplate.postForObject(baseURL + "/auth",
                authRequestMap , String.class);

        // then
        assertThat(response).contains(status(401)).contains(error("Unauthorized"));
    }

    @Test
    @DisplayName("Successful anonymous registration")
    public void test_successful_register_anonymous () {
        // when
        Account registeredAccount = this.restTemplate.postForObject(baseURL + "/register", accountDto, Account.class);

        // then
        assertThat(registeredAccount).isNotNull();
        List<Account> accounts = accountService.findAll();

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(1)).extracting("username", "email", "password", "role", "address")
                .containsExactly(registeredAccount.getUsername(), registeredAccount.getEmail(),
                        registeredAccount.getPassword(), registeredAccount.getRole(), registeredAccount.getAddress());
        assertThat(accounts.get(1).getAudit().getCreatedBy()).isEqualTo(accountDto.getUsername());
    }

    @Test
    @DisplayName("Successful registration by known user")
    public void test_successful_register_knownUser () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);

        // when
        Account registeredAccount = this.restTemplate.exchange(
                baseURL + "/register", HttpMethod.POST,
                new HttpEntity<>(accountDto, headers), Account.class).getBody();

        // then
        assertThat(registeredAccount).isNotNull();
        List<Account> accounts = accountService.findAll();

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(1)).extracting("username", "email", "password", "role", "address")
                .containsExactly(registeredAccount.getUsername(), registeredAccount.getEmail(),
                        registeredAccount.getPassword(), registeredAccount.getRole(), registeredAccount.getAddress());
        assertThat(accounts.get(1).getAudit().getCreatedBy()).isEqualTo(adminFullDto.getUsername());
    }

    @Test
    @DisplayName("Failed registration - missing field")
    public void test_failed_register () {
        // given
        AccountDto accountDto1 = AccountDto.builder()
                .username(accountDto.getUsername())
                .email(accountDto.getEmail()).build();

        // when
        String response = this.restTemplate.postForObject(baseURL + "/register",
                accountDto1 , String.class);

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Successful view own account")
    public void test_successful_viewSelfAccount () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);

        // when
        Account result = this.restTemplate.exchange(
                baseURL + "/account/view", HttpMethod.GET,
                new HttpEntity<>(headers), Account.class).getBody();

        // then
        assertThat(result).isInstanceOf(Account.class).extracting("username", "email", "role")
                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(), adminFullDto.getRole());
    }

    @Test
    @DisplayName("Successful update own account - core fields")
    public void test_successful_updateSelfAccount_core () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);
        AccountUpdateDto updatedAdmin = new AccountUpdateDto(
                AccountDto.builder().email("newAdminMail@mail.com").build(),
                new Address());
        
        // when
        Account response = this.restTemplate.exchange(baseURL + "/account/update", HttpMethod.PUT,
                new HttpEntity<>(updatedAdmin, headers), Account.class).getBody();

        // then
        assertThat(response).isInstanceOf(Account.class).extracting("username", "email", "role")
                .containsExactly(adminFullDto.getUsername(), updatedAdmin.getAccountDto().getEmail(),
                        adminFullDto.getRole());
    }

    @Test
    @DisplayName("Successful update own account - address")
    public void test_successful_updateSelfAccount_address () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);
        Address address = Address.builder()
                .streetAddress("27 Mobile Dr")
                .postalCode("M1M1M1")
                .country("Canada")
                .build();

        AccountUpdateDto accountUpdateDto = new AccountUpdateDto(AccountDto.builder().build(), address);

        // when
        Account response = this.restTemplate.exchange(baseURL + "/account/update", HttpMethod.PUT,
                new HttpEntity<>(accountUpdateDto, headers), Account.class).getBody();

        // then
        assertThat(response).isInstanceOf(Account.class)
                .extracting("username", "email", "role", "address")
                .containsExactly(adminFullDto.getUsername(), adminFullDto.getEmail(),
                        adminFullDto.getRole(), address);
    }

    @Test
    @DisplayName("Failed update own account")
    public void test_failed_updateSelfAccount () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);
        AccountUpdateDto accountUpdateDto = new AccountUpdateDto(AccountDto.builder().build(), new Address());

        // when
        String response = this.restTemplate.exchange(baseURL + "/account/update", HttpMethod.PUT,
                new HttpEntity<>(accountUpdateDto, headers), String.class).getBody();

        // then
        assertThat(response).contains(status(403)).contains(error("Forbidden"));
    }

    @Test
    @DisplayName("Successful delete own account")
    public void test_successful_deleteAccount () throws JSONException {
        // given
        HttpHeaders headers = getTokenAsHeaders(authRequestMap);

        // when
        Account deletedAccount = this.restTemplate.exchange(baseURL + "/account/delete", HttpMethod.DELETE,
                new HttpEntity<>(headers), Account.class).getBody();

        // then
        List<Account> accounts = accountService.findAll();
        assertThat(accounts).isEmpty();
        assertThat(deletedAccount.toDto().toFullDto(deletedAccount.getRole())).isEqualTo(adminFullDto);
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
