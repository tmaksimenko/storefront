package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
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

    @Override
    public String createAccount(AccountDto accountDto) {
        Account account = new Account();

        account.setUsername(accountDto.getUsername());
        account.setEmail(accountDto.getEmail());
        account.setPassword(accountDto.getPassword());

        accountRepository.save(account);

        return "SUCCESS";
    }

    @Override
    public String updateAccount(Account oldAccount, Account account) {
        if (account.getEmail() != null) oldAccount.setEmail(account.getEmail());
        if (account.getPassword() != null) oldAccount.setPassword(account.getPassword());
        oldAccount.setLast_modified(new Timestamp(new Date().getTime()));

        accountRepository.save(oldAccount);

        return "SUCCESS";
    }

    @Override
    public String deleteAccount(String username) {
        accountRepository.deleteById(username);
        return "SUCCESS";
    }

}
