package com.locally.locally_backend_engine.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String jwtIssuer = "locally-user-app";
    private final String serviceJwtIssuer = "locally-user-service";

    private final long accessTokenExpirationTime = 1000 * 60 * 15; // 15 minutes
    private final long refreshTokenExpirationTime = 1000L * 60 * 60 * 24 * 30; // 30 days
    private final long serviceTokenExpirationTime = 1000 * 60 * 5; // 5 min

    @Value("${jwt.secret}")
    private String base64Secret;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(key)
                .compact();
    }

    public String retrieveSubject(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException ex) {
            // Extract subject even if token is expired
            return ex.getClaims().getSubject();
        }
    }

    public Long retrieveUserIdFromServiceToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!serviceJwtIssuer.equals(claims.getIssuer())) {
                throw new RuntimeException("Invalid service token issuer");
            }

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException ex) {
            return Long.parseLong(ex.getClaims().getSubject());
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String subject = retrieveSubject(token);
        return (subject.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    public boolean isRefreshTokenExpired(String token) {
        return isTokenExpired(token);
    }

    public Date getExpirationDate(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (ExpiredJwtException ex) {
            // Return the expiration from the expired token's claims
            return ex.getClaims().getExpiration();
        } catch (Exception ex) {
            throw new RuntimeException("Invalid JWT token", ex);
        }
    }

    public long getRefreshTokenExpirationTimeInMillis() {
        return refreshTokenExpirationTime;
    }

    public boolean isTokenValid(String token, String subject) {
        String tokenSubject = retrieveSubject(token);
        return subject.equals(tokenSubject) && !isTokenExpired(token);
    }

    public boolean isServiceTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!serviceJwtIssuer.equals(claims.getIssuer())) {
                return false;
            }

            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}