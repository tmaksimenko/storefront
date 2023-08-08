package com.tmaksimenko.storefront.auth;

import com.tmaksimenko.storefront.config.JwtSecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.jsonwebtoken.Claims.SUBJECT;
import static java.util.Calendar.MILLISECOND;

@Component
public class JwtUtils {

    public static final String CREATE_VALUE = "created", ROLES = "roles";

    final JwtSecurityConfig jwtSecurityConfig;

    final SecretKey key;

    @Autowired
    public JwtUtils(JwtSecurityConfig jwtSecurityConfig) {
        this.jwtSecurityConfig = jwtSecurityConfig;
        this.key = Keys.hmacShaKeyFor(jwtSecurityConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SUBJECT, userDetails.getUsername());
        claims.put(CREATE_VALUE, new Date());
        claims.put(ROLES, getEncryptedRoles(userDetails));
        return generateToken(claims);
    }

    public String generateToken(Map<String, Object> claims) {

        return Jwts.builder()
                .setHeaderParam("typ", Header.JWT_TYPE)
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(this.key)
                .compact();
    }

    private List<String> getEncryptedRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().
                stream()
                .map(GrantedAuthority::getAuthority)
                .map(s -> s.replace("ROLE_", ""))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        Boolean isMatchingUser = username.equals(userDetails.getUsername());
        if (checkExpiry(token))
            throw new ExpiredJwtException(
                    Jwts.parserBuilder().setSigningKey(this.key)
                            .build().parseClaimsJws(token).getHeader(),
                    getTokenClaims(token),
                    String.format("JWT expired at %s", getExpiration(token)));
        else return isMatchingUser;
    }

    public String getUsernameFromToken(String token) {
        return getTokenClaims(token).getSubject();
    }

    private Claims getTokenClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date generateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(MILLISECOND, jwtSecurityConfig.getExpiration());
        return calendar.getTime();
    }

    private Date getExpiration(String token) {
        return this.getTokenClaims(token).getExpiration();
    }

    private Boolean checkExpiry(String token) {
        return getExpiration(token).before(new Date());
    }

}
