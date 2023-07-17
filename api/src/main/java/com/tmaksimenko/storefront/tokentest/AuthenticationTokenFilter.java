package com.tmaksimenko.storefront.tokentest;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@RequiredArgsConstructor
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenHelper tokenUtils;

    private final UserDetailsService userDetailsService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader("X_AUTH_TOKEN");
        logger.info(authToken);
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String nextElement = headerNames.nextElement();
                logger.info("Header name: {} Header content: {}", nextElement, request.getHeader(nextElement));
            }
        }

        if (authToken != null) {
            String username = tokenUtils.getUsernameFromToken(authToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (Boolean.TRUE.equals(tokenUtils.validateToken(authToken, userDetails))) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    logger.info("doFilterInternal authentication -> {}", authentication);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else logger.info("NEXT ONE NULL");
        } else logger.info("AUTHTOKEN IS NULL -> {}", authToken);

        filterChain.doFilter(request, response);
    }


}
