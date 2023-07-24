package com.tmaksimenko.storefront.controller.user;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Address;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Tag(name = "User Operations")
@RestController
@RequestMapping("/account")
@EnableCaching
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "View account details",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @GetMapping("/view")
    public ResponseEntity<AccountFullDto> viewAccountDetails() {
        Account account;
        try {
            account = accountService.findByUsername(SecurityContextHolder.getContext()
                    .getAuthentication().getName()).get();
        } catch (AccountNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", e);
        }

        AccountFullDto accountFullDto = AccountFullDto.builder()
                .username(account.getUsername())
                .email(account.getEmail())
                .address(account.getAddress())
                .role(account.getRole())
                .audit(account.getAudit())
                .orderGetDtos(account.getOrders().stream().map(Order::toPlainDto).toList())
                .build();

        return new ResponseEntity<>(accountFullDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Update your account",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PutMapping("/update")
    public ResponseEntity<String> updateAccount(@RequestBody AccountDto accountDto, @RequestBody Address address) {
        Optional<Account> oldAccount = accountService.findByUsername(SecurityContextHolder
                .getContext().getAuthentication().getName());

        if (oldAccount.isEmpty())
            return new ResponseEntity<>("ACCOUNT NOT FOUND", HttpStatus.NOT_FOUND);

        AccountFullDto accountFullDto = AccountFullDto.builder()
                .address(address)
                .username(accountDto.getUsername())
                .email(accountDto.getEmail())
                .password(passwordEncoder.encode(accountDto.getPassword())).build();

        return accountService.updateAccount(oldAccount.get(), accountFullDto);

    }

    @Operation(
            summary = "Delete your account",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount() {
        return accountService.deleteAccount(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
