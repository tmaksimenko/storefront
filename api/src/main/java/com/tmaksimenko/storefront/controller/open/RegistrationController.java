package com.tmaksimenko.storefront.controller.open;


import com.tmaksimenko.storefront.dto.account.AccountCreateDto;
import com.tmaksimenko.storefront.dto.account.AccountDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.service.account.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Sign-in Portal")
@RestController
@RequestMapping("/register")
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationController {

    final AccountService accountService;
    final PasswordEncoder passwordEncoder;

    @PostMapping()
    public ResponseEntity<String> addAccount(@RequestBody AccountCreateDto accountCreateDto) {
        AccountDto accountDto = accountCreateDto.toFullDto(Role.ROLE_USER,
                Audit.builder().createdOn(LocalDateTime.now()).createdBy(accountCreateDto.getUsername()).build());
        accountDto.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
            return accountService.createAccount(accountDto, SecurityContextHolder.getContext().getAuthentication().getName());
        else return accountService.createAccount(accountDto, accountCreateDto.getUsername());
    }

}
