package com.tmaksimenko.storefront;

import com.tmaksimenko.storefront.auth.JwtUtils;
import com.tmaksimenko.storefront.config.JwtSecurityConfig;
import com.tmaksimenko.storefront.dto.account.AccountDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Claims.SUBJECT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    JwtUtils spyJwtUtils;

    AccountDto accountDto;

    @BeforeEach
    public void setup () {
        JwtSecurityConfig jwtSecurityConfig = Mockito.mock(JwtSecurityConfig.class);
        given(jwtSecurityConfig.getSecret()).willReturn("testStringMustBeThirtyTwoCharacters");
        spyJwtUtils = Mockito.spy(new JwtUtils(jwtSecurityConfig));
        accountDto = new AccountDto("testUser", "mail@mail.com", "password");
    }

    @Test
    public void test_failed_validateToken_expired () {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put(SUBJECT, accountDto.getUsername());
        claims.put(JwtUtils.CREATE_VALUE, new Date(0));
        claims.put(JwtUtils.ROLES, AuthorityUtils.createAuthorityList("USER"));
        String token = spyJwtUtils.generateToken(claims);

        User user = new User(accountDto.getUsername(), accountDto.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        // when, then
        assertThrows(ExpiredJwtException.class, () -> spyJwtUtils.validateToken(token, user));
    }

}
