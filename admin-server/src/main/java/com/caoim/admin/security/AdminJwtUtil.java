package com.caoim.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AdminJwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generate(Long adminId, String username, boolean superAdmin) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("adminId", adminId)
                .claim("superAdmin", superAdmin)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key())
                .compact();
    }

    public String username(String token) {
        return claims(token).getSubject();
    }

    public boolean valid(String token) {
        try {
            claims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
