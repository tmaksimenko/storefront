package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Cart;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> findAll();

    @SuppressWarnings("unused")
    Optional<Account> findById(Long id);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByLogin(String login);

    Account createAccount(AccountFullDto accountFullDto);

    Account updateAccount(AccountFullDto accountFullDto);

    Account addCart (Cart cart);

    @SuppressWarnings("unused")
    Account deleteAccount(Long id);

    Account deleteAccount(String login);

}
