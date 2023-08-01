package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountServiceImplementation implements AccountService {

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
    public Account createAccount (AccountFullDto accountFullDto) {
        if (!accountRepository.findByUsername(accountFullDto.getUsername()).isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCOUNT ALREADY EXISTS");

        return accountRepository.save(accountFullDto.toNewAccount());
    }

    @Override
    public Account updateAccount(AccountFullDto accountFullDto) {
        Account account;
        List<Account> tempAccountList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(accountFullDto.getUsername()))
            tempAccountList = accountRepository.findByUsername(accountFullDto.getUsername());
        if (tempAccountList.size() == 1)
            account = tempAccountList.get(0);
        else {
            if (ObjectUtils.isNotEmpty(accountFullDto.getEmail()))
                tempAccountList = accountRepository.findByEmail(accountFullDto.getEmail());
            if (tempAccountList.size() == 1)
                account = tempAccountList.get(0);
            else
                account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get(0);
        }

        if (ObjectUtils.isNotEmpty(accountFullDto.getUsername()))
            account.setUsername(accountFullDto.getUsername());
        if (ObjectUtils.isNotEmpty(accountFullDto.getEmail()))
            account.setEmail(accountFullDto.getEmail());
        if (ObjectUtils.isNotEmpty(accountFullDto.getPassword()))
            account.setPassword(accountFullDto.getPassword());
        if (ObjectUtils.isNotEmpty(accountFullDto.getAddress())) {
            Address newAddress = accountFullDto.getAddress();
            Address address = new Address();
            if (newAddress.getStreetAddress() != null)
                address.setStreetAddress(newAddress.getStreetAddress());
            if (newAddress.getPostalCode() != null)
                address.setPostalCode(newAddress.getPostalCode());
            if (newAddress.getCountry() != null)
                address.setCountry(newAddress.getCountry());
            account.setAddress(address);
        }

        if (ObjectUtils.isNotEmpty(accountFullDto.getRole()))
            if (! (accountFullDto.getRole().equals(account.getRole())))
                account.setRole(accountFullDto.getRole());

        return account;
    }

    @Override
    public Account deleteAccount(Long id) {
        Optional<Account> account = accountRepository.findById(id);
        if (account.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());
        accountRepository.delete(account.get());
        return account.get();
    }

    @Override
    public Account deleteAccount(String login) {
        Optional<Account> account = this.findByLogin(login);
        if (account.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());
        accountRepository.delete(account.get());
        return account.get();
    }
}
