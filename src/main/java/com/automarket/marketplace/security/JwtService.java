package com.automarket.marketplace.security;

import com.automarket.marketplace.config.JwtProperties;
import com.automarket.marketplace.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(jwtProperties.secret()).getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
            .subject(user.getEmail())
            .claims(Map.of(
                "uid", user.getId().toString(),
                "role", user.getRole().name()
            ))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String value = parse(token).get("uid", String.class);
        return UUID.fromString(value);
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        Claims claims = parse(token);
        String subject = claims.getSubject();
        Date expiration = claims.getExpiration();
        return subject.equalsIgnoreCase(principal.getEmail()) && expiration.after(new Date());
    }

    public long accessTokenExpiresInSeconds() {
        return jwtProperties.accessTokenExpirationMinutes() * 60;
    }

    public long refreshTokenExpirationDays() {
        return jwtProperties.refreshTokenExpirationDays();
    }

    private String normalizeSecret(String secret) {
        String fallback = "replace-me-with-a-secure-32-byte-minimum-secret";
        String source = (secret == null || secret.isBlank()) ? fallback : secret;
        if (source.length() >= 32) {
            return source;
        }
        StringBuilder sb = new StringBuilder(source);
        while (sb.length() < 32) {
            sb.append('_');
        }
        return sb.toString();
    }
}

