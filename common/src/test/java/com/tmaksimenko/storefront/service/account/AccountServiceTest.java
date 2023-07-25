package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    AccountService accountService;

    Account account;

    @BeforeEach
    public void setup_each () {
        accountService = new AccountServiceImpl(accountRepository);
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

    @DisplayName("Test for non-empty findAll")
    @Test
    public void test_successful_findAll () {
        // given
        Account account1 = account.toBuilder()
                        .id(2L)
                        .username("testUser1")
                        .email("testEmail1@mail.com")
                        .build();

        given(accountRepository.findAll()).willReturn(List.of(account, account1));

        // when
        List<Account> accounts = accountService.findAll();

        // then
        assertThat(accounts).isNotNull();
        assertThat(accounts).contains(account);
        assertThat(accounts).contains(account1);
        assertThat(accounts).hasSize(2);
    }

    @DisplayName("Test for non-empty findAll")
    @Test
    public void test_empty_findAll () {
        // given
        given(accountRepository.findAll()).willReturn(new ArrayList<>());

        // when
        List<Account> accounts = accountService.findAll();

        // then
        assertThat(accounts).isNotNull();
        assertThat(accounts).hasSize(0);
    }

    @DisplayName("Test for successful findById")
    @Test
    public void test_successful_findById () {
        // given
        given(accountRepository.findById(account.getId())).willReturn(Optional.of(account));

        // when
        Optional<Account> account1 = accountService.findById(account.getId());

        // then
        assertThat(account1).isPresent();
        assertThat(account1.get()).isEqualTo(account);
    }

    @DisplayName("Test for failed findById")
    @Test
    public void test_failed_findById () {
        // given
        given(accountRepository.findById(2L)).willReturn(Optional.empty());

        // when
        Optional<Account> account1 = accountService.findById(account.getId());

        // then
        assertThat(account1).isEmpty();
    }

    @DisplayName("Test for successful findByUsername")
    @Test
    public void test_successful_findByUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByUsername(account.getUsername());

        // then
        assertThat(account1.isPresent());
        assertThat(account1.get().equals(account));
    }

    @DisplayName("Test for failed findByUsername")
    @Test
    public void test_failed_findByUsername () {
        // given
        given(accountRepository.findByUsername("notTestUser")).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByUsername(account.getUsername());

        // then
        assertThat(account1.isEmpty());
    }

    @DisplayName("Test for successful findByEmail")
    @Test
    public void test_successful_findByEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByEmail(account.getEmail());

        // then
        assertThat(account1.isPresent());
        assertThat(account1.get().equals(account));
    }

    @DisplayName("Test for failed findByEmail")
    @Test
    public void test_failed_findByEmail () {
        // given
        given(accountRepository.findByEmail("notTestMail@mail.com")).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByEmail(account.getEmail());

        // then
        assertThat(account1.isEmpty());
    }

    @DisplayName("Test for successful findByLogin with username")
    @Test
    public void test_successful_findByLogin_withUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getUsername());

        // then
        assertThat(account1.isPresent());
        assertThat(account1.equals(account));
    }

    @DisplayName("Test for failed findByLogin with username")
    @Test
    public void test_failed_findByLogin_withUsername () {
        // given
        given(accountRepository.findByUsername("notTestUser")).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getUsername());

        // then
        assertThat(account1.isEmpty());
    }

    @DisplayName("Test for successful findByLogin with email")
    @Test
    public void test_successful_findByLogin_withEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getEmail());

        // then
        assertThat(account1.isPresent());
        assertThat(account1.equals(account));
    }

    @DisplayName("Test for failed findByLogin with email")
    @Test
    public void test_failed_findByLogin_withEmail () {
        // given
        given(accountRepository.findByEmail("notTestMail@mail.com")).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getEmail());

        // then
        assertThat(account1.isEmpty());
    }





}
