package com.silkroad.market.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility service for creating and validating JSON Web Tokens (JWTs).
 * <p>
 * Encapsulates token generation, claim extraction, and expiration checks.
 */
@Service
public class JwtService {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "this-is-a-very-secret-key-for-market-app-jwt-please-change".getBytes());

    private final long expirationMs = 24 * 60 * 60 * 1000;

    /**
     * Generates a JWT token for the given username and role.
     *
     * @param username subject of the token
     * @param role     user role to include as a claim
     * @return signed JWT token string
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}