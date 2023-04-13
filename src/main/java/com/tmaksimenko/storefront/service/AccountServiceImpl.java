package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    final AccountRepository accountRepository;

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> findById(String username) {
        return accountRepository.findById(username);
    }

}
