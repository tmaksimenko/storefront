package com.tmaksimenko.storefront.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmaksimenko.storefront.auth.JwtUtils;
import com.tmaksimenko.storefront.controller.login.RegistrationController;
import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.service.account.AccountService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {

    MockMvc mockMvc;

    @MockBean
    AccountService accountService;

    AccountDto accountDto;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @SuppressWarnings("unused")
    @MockBean
    BCryptPasswordEncoder passwordEncoder;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    UserDetailsService userDetailsService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        passwordEncoder = new BCryptPasswordEncoder();
        accountDto = AccountDto.builder().username("testUser")
                .password("password")
                .email("mail@mail.com").build();
        given(jwtUtils.getUsernameFromToken(Mockito.anyString())).willReturn("testUser");
        given(jwtUtils.validateToken(Mockito.any(), Mockito.any())).willReturn(true);
        given(userDetailsService.loadUserByUsername("testUser")).willReturn(
                new org.springframework.security.core.userdetails.User(
                    accountDto.getUsername(),
                    accountDto.getPassword(),
                    AuthorityUtils.createAuthorityList(Role.ROLE_USER.name())));
    }

    @Test
    @DisplayName("Successful addAccount - known")
    public void test_successful_addAccount_known () throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(accountDto.getUsername(), accountDto.getPassword()));
        given(accountService.createAccount(Mockito.any(AccountFullDto.class)))
                .willAnswer(i -> i.getArgument(0, AccountFullDto.class).toFullDto
                                (Role.ROLE_USER, "RegistrationControllerTest").toNewAccount());

        // when, then
        this.mockMvc.perform(post("/register")
                        .content(new ObjectMapper().valueToTree(accountDto).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    @DisplayName("Successful addAccount - anonymous")
    public void test_successful_addAccount_anonymous () throws Exception{
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("anonymous", "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        given(accountService.createAccount(Mockito.any(AccountFullDto.class)))
                .willAnswer(i -> i.getArgument(0, AccountFullDto.class).toFullDto
                        (Role.ROLE_USER, "RegistrationControllerTest").toNewAccount());

        // when, then
        this.mockMvc.perform(post("/register")
                        .content(new ObjectMapper().valueToTree(accountDto).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.audit.createdBy").value("RegistrationControllerTest"));
    }

    @Test
    @DisplayName("Failed addAccount")
    public void test_failed_addAccount () throws Exception {
        // given
        AccountDto accountDto1 = AccountDto.builder()
                .username(accountDto.getUsername())
                .email(accountDto.getEmail()).build();

        // when, then
        this.mockMvc.perform(post("/register")
                        .content(new ObjectMapper().valueToTree(accountDto1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertEquals(
                        "403 FORBIDDEN \"ACCOUNT REQUIRES ALL FIELDS\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }


}
