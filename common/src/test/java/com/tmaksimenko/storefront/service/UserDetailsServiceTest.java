package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.account.UserDetailsServiceImplementation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {
    @Mock
    AccountService accountService;

    UserDetailsService userDetailsService;

    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    Account account;

    @BeforeEach
    public void setup () {
        userDetailsService = new UserDetailsServiceImplementation(accountService);
        account = Account.builder()
                .id(1L)
                .audit(new Audit("Test"))
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
    }

    @Test
    @DisplayName("Successful loadUserByUsername")
    public void test_successful_loadUserByUsername () {
        // given
        given(accountService.findByLogin(account.getUsername())).willReturn(Optional.of(account));

        // when
        UserDetails user = userDetailsService.loadUserByUsername(account.getUsername());

        // then
        assertThat(user.getUsername()).isEqualTo(account.getUsername());
        assertThat(user.getPassword()).isEqualTo(account.getPassword());
        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority(account.getRole().name())));
    }

    @Test
    @DisplayName("Failed loadUserByUsername")
    public void test_failed_loadUserByUsername () {
        // given
        given(accountService.findByLogin(account.getUsername())).willReturn(Optional.empty());

        // when
        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userDetailsService.loadUserByUsername(account.getUsername()));

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT NOT FOUND")
                .hasCause(new AccountNotFoundException());
    }


}
