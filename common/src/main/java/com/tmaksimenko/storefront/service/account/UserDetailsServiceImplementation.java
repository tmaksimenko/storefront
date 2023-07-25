package com.tmaksimenko.storefront.service.account;

import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserDetailsServiceImplementation implements UserDetailsService {

    final AccountService accountService;

    @Override
    public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {

        Optional<Account> searchResult = accountService.findByLogin(username);

        if (searchResult.isPresent()) {
            Account account = searchResult.get();
            return new org.springframework.security.core.userdetails.User(
                    account.getUsername(),
                    account.getPassword(),
                    AuthorityUtils.createAuthorityList(account.getRole().toString())
            );
        } else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ACCOUNT NOT FOUND",
                    new AccountNotFoundException());
    }

}
