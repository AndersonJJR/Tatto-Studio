package com.studio.tattoo.security;

import com.studio.tattoo.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration-hours}")
    private Long expirationHours;

    private static final String ISSUER = "tattoo-studio-api";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(expirationHours, ChronoUnit.HOURS);

            return Jwts.builder()
                    .issuer(ISSUER)
                    .subject(user.getEmail())
                    .claim("id", user.getId() != null ? user.getId().toString() : null)
                    .claim("role", user.getRole().name())
                    .claim("name", user.getName())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}
