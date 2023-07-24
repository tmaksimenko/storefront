package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@EnableCaching
@CacheConfig(cacheNames = "accounts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountServiceImpl implements AccountService {

    final AccountRepository accountRepository;

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Cacheable
    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Cacheable
    @Override
    public Optional<Account> findByUsername(String username) {
        return Optional.ofNullable(accountRepository.findByUsername(username).isEmpty() ?
                null : accountRepository.findByUsername(username).get(0));
    }

    @Cacheable
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
    public ResponseEntity<String> createAccount(AccountFullDto accountFullDto) {
        if (!accountRepository.findByUsername(accountFullDto.getUsername()).isEmpty())
            return new ResponseEntity<>("ACCOUNT ALREADY EXISTS", HttpStatus.FORBIDDEN);

        if (Arrays.stream(Role.values()).noneMatch((role) ->
                role.equals(accountFullDto.getRole())))
            return new ResponseEntity<>("ROLE NOT FOUND", HttpStatus.FORBIDDEN);

        accountRepository.save(Account.builder()
                .username(accountFullDto.getUsername())
                .email(accountFullDto.getEmail())
                .password(accountFullDto.getPassword())
                .role(accountFullDto.getRole())
                .audit(accountFullDto.getAudit())
                .build());

        return new ResponseEntity<>("SUCCESS", HttpStatus.CREATED);
    }

    @CachePut
    @Override
    public ResponseEntity<String> updateAccount(Account oldAccount, AccountFullDto accountFullDto) {
        List<String> changedList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(accountFullDto.getUsername()))  {
            oldAccount.setUsername(accountFullDto.getUsername());
            changedList.add("USERNAME");
        }
        if (!ObjectUtils.isEmpty(accountFullDto.getEmail())) {
            oldAccount.setEmail(accountFullDto.getEmail());
            changedList.add("EMAIL");
        }
        if (!ObjectUtils.isEmpty(accountFullDto.getPassword())) {
            oldAccount.setPassword(accountFullDto.getPassword());
            changedList.add("PASSWORD");
        }
        if (!ObjectUtils.isEmpty(accountFullDto.getAddress())) {
            oldAccount.setAddress(accountFullDto.getAddress());
            changedList.add("ADDRESS");
        }
        if (!(accountFullDto.getRole().equals(oldAccount.getRole()))) {
            if (Arrays.stream(Role.values()).anyMatch((role) ->
                    role.equals(accountFullDto.getRole()))) {
                oldAccount.setRole(accountFullDto.getRole());
                changedList.add("ROLE");
            } else return new ResponseEntity<>("INVALID ROLE GIVEN", HttpStatus.FORBIDDEN);
        }

        accountRepository.save(oldAccount);

        return new ResponseEntity<>(String.format("CHANGED FIELDS: %s", changedList), HttpStatus.OK);
    }

    @CacheEvict(key = "#id")
    @Override
    public ResponseEntity<String> deleteAccount(Long id) {
        if (accountRepository.findById(id).isEmpty())
            throw new AccountNotFoundException();
        accountRepository.deleteById(id);
        return new ResponseEntity<>("ACCOUNT DELETED", HttpStatus.OK);
    }

    @CacheEvict(key = "#login")
    @Override
    public ResponseEntity<String> deleteAccount(String login) {
        Optional<Account> optionalAccount = this.findByUsername(login);
        if (optionalAccount.isEmpty()) {
            optionalAccount = this.findByEmail(login);
            if (optionalAccount.isEmpty())
                throw new AccountNotFoundException();
        }

        accountRepository.deleteById(optionalAccount.get().getId());
        return new ResponseEntity<>("ACCOUNT DELETED", HttpStatus.OK);
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
