package com.tmaksimenko.storefront.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtSecurityConfig {

    @Value("${jwt.secret}")
    String secret;

    @Value("${jwt.expiration}")
    Integer expiration;

}
