package com.typingtutor.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private static final long CHANGE_PASSWORD_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generateToken(String username) {
        return generateToken(username, "USER");
    }

    public String generateChangePasswordToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("purpose", "CHANGE_PASSWORD")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + CHANGE_PASSWORD_EXPIRY_MS))
                .signWith(key())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        Object role = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().get("role");
        return role != null ? role.toString() : "USER";
    }

    /** Returns username if the token is a valid CHANGE_PASSWORD token, null otherwise. */
    public String extractChangePasswordUsername(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
            Object purpose = claims.get("purpose");
            if (!"CHANGE_PASSWORD".equals(purpose)) return null;
            if (claims.getExpiration().before(new Date())) return null;
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
            return claims.getSubject().equals(userDetails.getUsername())
                    && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
