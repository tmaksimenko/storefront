package com.tmaksimenko.storefront.controller;

import com.tmaksimenko.storefront.dto.AccountDto;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountController {

    @Autowired
    AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<List<Account>> findAll() {
        List<Account> accounts = accountService.findAll();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<Account> findById(@RequestParam String username) {
        Optional<Account> optionalAccount = accountService.findById(username);
        return ResponseEntity.of(optionalAccount);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(@RequestBody AccountDto accountDto) {
        String result = accountService.createAccount(accountDto);
        return new ResponseEntity<> (result, HttpStatus.OK);
    }

}
