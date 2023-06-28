package com.tmaksimenko.storefront.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/accounts/**").permitAll()
                                .requestMatchers("accounts").permitAll()
                                .requestMatchers("/orders/**").permitAll()
                                .requestMatchers("/products/**").permitAll()
                                .anyRequest().authenticated()
                        );
        return httpSecurity.build();
    }

}
