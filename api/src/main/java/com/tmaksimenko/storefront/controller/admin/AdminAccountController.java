package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.dto.account.AccountCreateDto;
import com.tmaksimenko.storefront.dto.account.AccountFullDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.model.account.Account;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Tag(name = "Administrator Utilities")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@EnableCaching
@CacheConfig(cacheNames = "accounts")
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminAccountController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;

    @Operation(summary = "View all accounts", parameters =
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Auth-Token",
                    required = true,
                    description = "JWT Token, can be generated in auth controller /auth"))
    @GetMapping("/all")
    public ResponseEntity<List<AccountFullDto>> viewAll() {
        List<Account> accounts = accountService.findAll();
        List<AccountFullDto> accountFullDtos = accounts.stream().map(
                x -> (AccountFullDto)(AccountFullDto.builder()
                        .username(x.getUsername())
                        .email(x.getEmail())
                        .orderGetDtos(x.getOrders().stream().map(Order::toPlainDto).toList())
                        .build())
            ).toList();
        return ResponseEntity.ok(accountFullDtos);
    }

    @Operation(summary = "View individual account", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @GetMapping("/view")
    public ResponseEntity<AccountFullDto> viewAccountDetails(@RequestParam String login) {
        Optional<Account> optionalAccount = accountService.findByLogin(login);

        if (optionalAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND", new AccountNotFoundException());

        return ResponseEntity.ok(optionalAccount.get().toDto());
    }

    @Operation(summary = "Add an account", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PostMapping("/add")
    public ResponseEntity<AccountFullDto> addAccount(@RequestBody AccountCreateDto accountCreateDto) {
        accountCreateDto.setPassword(passwordEncoder.encode(accountCreateDto.getPassword()));

        if (Arrays.stream(Role.values()).noneMatch((role) ->
                role.equals(accountCreateDto.getRole())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ROLE NOT FOUND");

        return ResponseEntity.ok(accountService.createAccount
                (accountCreateDto.toFullDto(
                        SecurityContextHolder.getContext().getAuthentication().getName()))
                .toDto());
    }

    @Operation(summary = "Update an account", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @Cacheable
    @PutMapping("/update")
    @SuppressWarnings("all")
    public ResponseEntity<AccountFullDto> updateAccount(@RequestBody AccountFullDto accountFullDto) {
        return ResponseEntity.ok(accountService.updateAccount(accountFullDto).toDto());
    }

    @Operation(summary = "Delete an account", parameters =
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth"))
    @DeleteMapping("/delete")
    public ResponseEntity<Account> deleteAccount(@RequestParam String login) {
        return ResponseEntity.ok(accountService.deleteAccount(login));
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(allEntries = true)
    public void emptyCache () {
    }

}
