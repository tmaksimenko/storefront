package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.model.Account;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> findAll();

    Optional<Account> findById(Long id);

    Optional<Account> findByUsername(String username);


    Optional<Account> findByEmail(String email);

    ResponseEntity<String> createAccount(AccountDto accountDto);

    ResponseEntity<String> updateAccount(Account oldAccount, AccountDto accountDto);

    ResponseEntity<String> deleteAccount(Long id);

}
