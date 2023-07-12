package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountServiceImpl implements AccountService {

    final AccountRepository accountRepository;

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return Optional.ofNullable(accountRepository.findByUsername(username).isEmpty() ?
                null : accountRepository.findByUsername(username).get(0));
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return Optional.ofNullable(accountRepository.findByEmail(email).isEmpty() ?
                null : accountRepository.findByEmail(email).get(0));
    }

    @Override
    public Optional<Account> findByLogin (String login) {
        return this.findByUsername(login).isEmpty() ?
                this.findByEmail(login) : this.findByUsername(login);
    }

    @Override
    public ResponseEntity<String> createAccount(AccountDto accountDto) {
        if (!accountRepository.findByUsername(accountDto.getUsername()).isEmpty())
            return new ResponseEntity<>("ACCOUNT ALREADY EXISTS", HttpStatus.FORBIDDEN);

        Account account = new Account();

        if (accountDto.getEmail().isEmpty() || accountDto.getPassword().isEmpty())
            return new ResponseEntity<>("ACCOUNT NEEDS EMAIL AND PASSWORD", HttpStatus.FORBIDDEN);

        account.setUsername(accountDto.getUsername());
        account.setEmail(accountDto.getEmail());
        account.setPassword(accountDto.getPassword());

        accountRepository.save(account);

        return new ResponseEntity<>("SUCCESS", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> updateAccount(Account oldAccount, AccountDto accountDto) {
        List<String> changedList = new ArrayList<>();

        if (!StringUtils.isEmpty(accountDto.getUsername()))  {
            oldAccount.setUsername(accountDto.getUsername());
            changedList.add("USERNAME");
        }
        if (!StringUtils.isEmpty(accountDto.getEmail())) {
            oldAccount.setEmail(accountDto.getEmail());
            changedList.add("EMAIL");
        }
        if (!StringUtils.isEmpty(accountDto.getPassword())) {
            oldAccount.setPassword(accountDto.getPassword());
            changedList.add("PASSWORD");
        }

        accountRepository.save(oldAccount);

        return new ResponseEntity<>(String.format("CHANGED FIELDS: %s", changedList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteAccount(Long id) {
        if (accountRepository.findById(id).isEmpty())
            throw new AccountNotFoundException();
        accountRepository.deleteById(id);
        return new ResponseEntity<>("ACCOUNT DELETED", HttpStatus.OK);
    }

}
