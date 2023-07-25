package com.tmaksimenko.storefront.controller.user;

import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "User Operations")
@RestController
@EnableCaching
@CacheConfig(cacheNames = "accounts")
@RequestMapping("/account")
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
    @Cacheable("accounts")
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
    public ResponseEntity<Account> updateAccount(@RequestBody AccountDto accountDto, @RequestBody Address address) {

        AccountFullDto accountFullDto = AccountFullDto.builder()
                .address(address)
                .username(accountDto.getUsername())
                .email(accountDto.getEmail())
                .password(passwordEncoder.encode(accountDto.getPassword())).build();

        return ResponseEntity.ok(accountService.updateAccount(accountFullDto));
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
    public ResponseEntity<Account> deleteAccount() {
        return ResponseEntity.ok(accountService.deleteAccount(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
