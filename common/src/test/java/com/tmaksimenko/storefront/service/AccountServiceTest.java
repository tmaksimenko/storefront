package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.repository.AccountRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.account.AccountServiceImplementation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    AccountRepository accountRepository;

    AccountService accountService;

    AccountService spyAccountService;

    Account account;

    @BeforeEach
    public void setup () {
        accountService = new AccountServiceImplementation(accountRepository);
        spyAccountService = Mockito.spy(accountService);
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
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword()));
    }

    @Test
    @DisplayName("Successful findAll")
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
        assertThat(accounts).isNotEmpty().contains(account).contains(account1).hasSize(2);
    }

    @Test
    @DisplayName("Empty findAll")
    public void test_failed_findAll () {
        // given
        given(accountRepository.findAll()).willReturn(new ArrayList<>());

        // when
        List<Account> accounts = accountService.findAll();

        // then
        assertThat(accounts).isNotNull().hasSize(0);
    }

    @Test
    @DisplayName("Successful findById")
    public void test_successful_findById () {
        // given
        given(accountRepository.findById(account.getId())).willReturn(Optional.of(account));

        // when
        Optional<Account> account1 = accountService.findById(account.getId());

        // then
        assertThat(account1).isPresent().get().isSameAs(account);
    }

    @Test
    @DisplayName("Failed findById - not found")
    public void test_failed_findById () {
        // given
        given(accountRepository.findById(account.getId())).willReturn(Optional.empty());

        // when
        Optional<Account> account1 = accountService.findById(account.getId());

        // then
        assertThat(account1).isEmpty();
    }

    @Test
    @DisplayName("Successful findByUsername")
    public void test_successful_findByUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByUsername(account.getUsername());

        // then
        assertThat(account1).isPresent().get().isSameAs(account);
    }

    @Test
    @DisplayName("Failed findByUsername - not found")
    public void test_failed_findByUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByUsername(account.getUsername());

        // then
        assertThat(account1).isEmpty();
    }

    @Test
    @DisplayName("Successful findByEmail")
    public void test_successful_findByEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByEmail(account.getEmail());

        // then
        assertThat(account1).isPresent().get().isSameAs(account);
    }

    @Test
    @DisplayName("Failed findByEmail - not found")
    public void test_failed_findByEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByEmail(account.getEmail());

        // then
        assertThat(account1).isEmpty();
    }

    @Test
    @DisplayName("Successful findByLogin - with username")
    public void test_successful_findByLogin_withUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getUsername());

        // then
        assertThat(account1).isPresent().get().isSameAs(account);
    }

    @Test
    @DisplayName("Failed findByLogin - with username")
    public void test_failed_findByLogin_withUsername () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getUsername());

        // then
        assertThat(account1).isEmpty();
    }

    @Test
    @DisplayName("Successful findByLogin - with email")
    public void test_successful_findByLogin_withEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getEmail());

        // then
        assertThat(account1).isPresent().get().isSameAs(account);
    }

    @Test
    @DisplayName("Failed findByLogin - with email")
    public void test_failed_findByLogin_withEmail () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of());

        // when
        Optional<Account> account1 = accountService.findByLogin(account.getEmail());

        // then
        assertThat(account1).isEmpty();
    }

    @Test
    @DisplayName("Successful createAccount")
    public void test_successful_createAccount () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of());
        given(accountRepository.save(account.toDto().toNewAccount())).willReturn(account.toDto().toNewAccount());

        // when
        accountService.createAccount(account.toDto());

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(account.toDto().toNewAccount());
    }

    @Test
    @DisplayName("Failed createAccount - account already exists")
    public void test_failed_createAccount_alreadyExists () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccount(account.toDto()));

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT ALREADY EXISTS");
    }

    @Test
    @DisplayName("Changed updateAccount - using username")
    public void test_updateAccount_username_allChanged () {
        // given
        Account account1 = account.toBuilder()
                .email("newTestMail@mail.com")
                .password(passwordEncoder.encode("newPassword"))
                .address(account.getAddress().toBuilder().postalCode("M2M2M2").build())
                .role(Role.ROLE_STAFF)
                .build();
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Account account2 = accountService.updateAccount(account1.toDto());

        // then
        assertThat(account2).isEqualTo(account1);
    }

    @Test
    @DisplayName("Unchanged updateAccount - using username")
    public void test_updateAccount_username_noneChanged () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Account account1 = accountService.updateAccount(account.toDto());

        // then
        assertThat(account1).isEqualTo(account);
    }

    @Test
    @DisplayName("Changed updateAccount - using email")
    public void test_updateAccount_email_allChanged () {
        // given
        Account account1 = account.toBuilder()
                .username("newTestUser")
                .password(passwordEncoder.encode("newPassword"))
                .address(account.getAddress().toBuilder().postalCode("M2M2M2").build())
                .role(Role.ROLE_STAFF)
                .build();
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Account account2 = accountService.updateAccount(account1.toDto());

        // then
        assertThat(account2).isEqualTo(account1);
    }

    @Test
    @DisplayName("Unchanged updateAccount - using email")
    public void test_updateAccount_email_noneChanged () {
        // given
        given(accountRepository.findByEmail(account.getEmail())).willReturn(List.of(account));

        // when
        Account account1 = accountService.updateAccount(account.toDto());

        // then
        assertThat(account1).isEqualTo(account);
    }

    @Test
    @DisplayName("Changed updateAccount - using Context")
    public void test_updateAccount_context_allChanged () {
        // given
        Account account1 = account.toBuilder()
                .username("newTestUser")
                .email("newTestMail@mail.com")
                .password(passwordEncoder.encode("newPassword"))
                .address(account.getAddress().toBuilder().postalCode("M2M2M2").build())
                .role(Role.ROLE_STAFF)
                .build();

        given(accountRepository.findByUsername(account1.getUsername())).willReturn(List.of());
        given(accountRepository.findByEmail(account1.getEmail())).willReturn(List.of());
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Account account2 = accountService.updateAccount(account1.toDto());

        // then
        assertThat(account2).isEqualTo(account1);
    }

    @Test
    @DisplayName("Unchanged updateAccount - using Context")
    public void test_updateAccount_context_noneChanged () {
        // given
        given(accountRepository.findByUsername(account.getUsername())).willReturn(List.of(account));

        // when
        Account account1 = accountService.updateAccount(account.toDto());

        // then
        assertThat(account1).isEqualTo(account);
    }

    @Test
    @DisplayName("Successful deleteAccount - using id")
    public void test_successful_deleteAccount_id () {
        // given
        given(accountRepository.findById(account.getId())).willReturn(Optional.of(account));

        // when
        Account account1 = accountService.deleteAccount(account.getId());

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).delete(captor.capture());
        assertThat(account).isSameAs(captor.getValue());
        assertThat(account1).isEqualTo(account.toDto().toNewAccount());
    }

    @Test
    @DisplayName("Successful deleteAccount - using username")
    public void test_successful_deleteAccount_username () {
        // given
        given(spyAccountService.findByLogin(account.getUsername())).willReturn(Optional.of(account));

        // when
        Account account1 = spyAccountService.deleteAccount(account.getUsername());

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).delete(captor.capture());
        assertThat(account).isSameAs(captor.getValue());
        assertThat(account1).isEqualTo(account.toDto().toNewAccount());
    }

    @Test
    @DisplayName("Successful deleteAccount - using email")
    public void test_successful_deleteAccount_email () {
        // given
        given(spyAccountService.findByLogin(account.getEmail())).willReturn(Optional.of(account));

        // when
        Account account1 = spyAccountService.deleteAccount(account.getEmail());

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).delete(captor.capture());
        assertThat(account).isSameAs(captor.getValue());
        assertThat(account1).isEqualTo(account.toDto().toNewAccount());
    }

    @Test
    @DisplayName("Failed deleteAccount - using id")
    public void test_failed_deleteAccount_id () {
        // given
        given(accountRepository.findById(account.getId())).willReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> spyAccountService.deleteAccount(account.getId()));

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT NOT FOUND")
                .hasCause(new AccountNotFoundException());
    }

    @Test
    @DisplayName("Failed deleteAccount - using username")
    public void test_failed_deleteAccount_username () {
        // given
        given(spyAccountService.findByLogin(account.getUsername())).willReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> spyAccountService.deleteAccount(account.getUsername()));

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT NOT FOUND")
                .hasCause(new AccountNotFoundException());
    }

    @Test
    @DisplayName("Failed deleteAccount - using email")
    public void test_failed_deleteAccount_email () {
        // given
        given(spyAccountService.findByLogin(account.getEmail())).willReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> spyAccountService.deleteAccount(account.getEmail()));

        // then
        assertThat(exception).hasMessageContaining("ACCOUNT NOT FOUND")
                .hasCause(new AccountNotFoundException());
    }

}
