package com.tmaksimenko.storefront.controller.login;

import com.tmaksimenko.storefront.dto.auth.AuthRequest;
import com.tmaksimenko.storefront.dto.auth.AuthResponse;
import com.tmaksimenko.storefront.auth.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Login Portal")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {

    final AuthenticationManager authenticationManager;

    final JwtUtils jwtUtils;

    final UserDetailsService accountDetailsService;

    @PostMapping
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest request) {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return ResponseEntity.ok(
                AuthResponse.builder()
                .login(request.getLogin())
                .token(jwtUtils.generateToken(
                                accountDetailsService.loadUserByUsername(request.getLogin())))
                .build());
    }

}
