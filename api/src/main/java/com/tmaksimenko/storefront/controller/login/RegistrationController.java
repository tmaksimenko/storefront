package com.tmaksimenko.storefront.controller.login;


import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Tag(name = "Login Portal")
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Creates a new account",
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "X-Auth-Token",
                            description = "JWT Token (optional)")
            })
    @PostMapping()
    public ResponseEntity<String> addAccount(@RequestBody AccountDto accountDto) {
        if (isEmpty(accountDto.getUsername()) ||
            isEmpty(accountDto.getEmail()) ||
            isEmpty(accountDto.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCOUNT REQUIRES ALL FIELDS");
        accountDto.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        if (SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser"))
            return accountService.createAccount(accountDto.toFullDto(Role.ROLE_USER,
                    accountDto.getUsername()));
        else return accountService.createAccount(accountDto.toFullDto(Role.ROLE_USER,
                    SecurityContextHolder.getContext().getAuthentication().getName()));
    }

}
