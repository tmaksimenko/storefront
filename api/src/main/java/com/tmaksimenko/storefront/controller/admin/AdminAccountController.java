package com.tmaksimenko.storefront.controller.admin;

import com.tmaksimenko.storefront.dto.account.AccountCreateDto;
import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Order;
import com.tmaksimenko.storefront.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Administrator Utilities")
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminAccountController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;


    @Operation(
            summary = "Views all accounts (admin only)",
            parameters = {
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-Auth-Token",
                    required = true,
                    description = "JWT Token, can be generated in auth controller /auth")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<AccountDto>> viewAll() {
        List<Account> accounts = accountService.findAll();
        List<AccountDto> accountDtos  = accounts.stream().map(
                x -> (AccountDto)(AccountDto.builder()
                        .username(x.getUsername())
                        .email(x.getEmail())
                        .orderGetDtos(x.getOrders().stream().map(Order::toPlainDto).toList())
                        .build())
        ).toList();
        return new ResponseEntity<>(accountDtos, HttpStatus.OK);
    }

    @Operation(
            summary = "Views individual account (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
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
                .role(optionalAccount.get().getRole())
                .audit(optionalAccount.get().getAudit())
                .orderGetDtos(optionalAccount.get().getOrders().stream().map(Order::toPlainDto).toList())
                .build();

        return new ResponseEntity<>(accountDto, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds an account (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addAccount(@RequestBody AccountCreateDto accountCreateDto) {
        accountCreateDto.setPassword(passwordEncoder.encode(accountCreateDto.getPassword()));
        return accountService.createAccount(accountCreateDto, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Operation(
            summary = "Updates an account (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    @SuppressWarnings("all")
    public ResponseEntity<String> updateAccount(@RequestBody AccountDto accountDto) {
        Optional<Account> oldAccount = accountService.findByUsername(accountDto.getUsername());

        if (oldAccount.isEmpty())
            oldAccount = accountService.findByEmail(accountDto.getEmail());

        if (oldAccount.isPresent())
            return accountService.updateAccount(oldAccount.get(), accountDto);

        return new ResponseEntity<>("ACCOUNT NOT FOUND", HttpStatus.NOT_FOUND);
    }

    @Operation(
            summary = "Deletes an account (admin only)",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            required = true,
                            description = "JWT Token, can be generated in auth controller /auth")
            })
    @PreAuthorize("hasRole('ADMIN')")
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
