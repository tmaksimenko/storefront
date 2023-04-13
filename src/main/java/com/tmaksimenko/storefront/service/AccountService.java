package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> findAll();

    Optional<Account> findById(String username);

    String createAccount(AccountDto accountDto);

}
