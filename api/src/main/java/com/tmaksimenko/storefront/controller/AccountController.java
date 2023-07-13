package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.AccountCreateDto;
import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.service.account.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountController {

    final AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<List<AccountDto>> viewAll() {
        List<Account> accounts = accountService.findAll();
        List<AccountDto> accountDtos  = accounts.stream().map(
                x -> AccountDto.builder()
                        .username(x.getUsername())
                        .email(x.getEmail())
                        .orders(x.getOrders().stream().map(Order::toPlainDto).toList()).build()
        ).toList();
        return new ResponseEntity<>(accountDtos, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<AccountDto> viewAccountDetails(@RequestParam String usernameOrEmail) {
        Optional<Account> optionalAccount = accountService.findByUsername(usernameOrEmail);

        if (optionalAccount.isEmpty())
            optionalAccount = accountService.findByEmail(usernameOrEmail);

        if (optionalAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());

        AccountDto accountDto = AccountDto.builder()
                .username(optionalAccount.get().getUsername())
                .email(optionalAccount.get().getEmail())
                .address(optionalAccount.get().getAddress())
                .audit(optionalAccount.get().getAudit())
                .orders(optionalAccount.get().getOrders().stream().map(Order::toPlainDto).toList())
                .build();

        return new ResponseEntity<>(accountDto, HttpStatus.OK);
    }

    @RequestMapping("")
    public ResponseEntity<String> defaultResponse() {
        return new ResponseEntity<>("This is a mock storefront built with Spring Boot!",
                HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAccount(@RequestBody AccountCreateDto accountCreateDto) {
        return accountService.createAccount(accountCreateDto);
    }

    @PutMapping("/update")
    @SuppressWarnings("all")
    public ResponseEntity<String> updateAccount(@RequestBody AccountCreateDto accountCreateDto) {
        Optional<Account> oldAccount = accountService.findByUsername(accountCreateDto.getUsername());

        if (oldAccount.isEmpty())
            oldAccount = accountService.findByEmail(accountCreateDto.getEmail());

        if (oldAccount.isPresent())
            return accountService.updateAccount(oldAccount.get(), accountCreateDto);

        return new ResponseEntity<>("ACCOUNT NOT FOUND", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@RequestParam Map<String,String> params) {

        if (!params.containsKey("username"))
            return new ResponseEntity<>("NO USERNAME GIVEN", HttpStatus.NOT_FOUND);

        try {
            return accountService
                    .deleteAccount(
                            accountService.findByUsername(params.get("username"))
                            .orElseThrow(AccountNotFoundException::new).getId());
        } catch (AccountNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", e);
        }
    }

}
