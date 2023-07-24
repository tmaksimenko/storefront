package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.model.account.Account;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> findAll();

    @SuppressWarnings("unused")
    Optional<Account> findById(Long id);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByLogin(String login);

    ResponseEntity<String> createAccount(AccountFullDto accountFullDto);

    ResponseEntity<String> updateAccount(Account oldAccount, AccountFullDto accountFullDto);

    @SuppressWarnings("unused")
    ResponseEntity<String> deleteAccount(Long id);

    @SuppressWarnings("unused")
    ResponseEntity<String> deleteAccount(String login);

}
