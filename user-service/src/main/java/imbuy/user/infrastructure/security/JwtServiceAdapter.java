package imbuy.user.infrastructure.security;

import imbuy.user.application.port.out.TokenPort;
import imbuy.user.domain.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtServiceAdapter implements TokenPort {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.access-token-validity:3600000}")
    private long accessValidityMs;

    @Value("${app.security.jwt.refresh-token-validity:1209600000}")
    private long refreshValidityMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccess(User user) {
        return build(user, accessValidityMs, "ACCESS");
    }

    @Override
    public String generateRefresh(User user) {
        return build(user, refreshValidityMs, "REFRESH");
    }

    @Override
    public boolean isValid(String token, String username) {
        return extractUsername(token).equals(username) && !isExpired(token);
    }

    @Override
    public boolean isRefresh(String token) {
        return "REFRESH".equalsIgnoreCase(extractClaim(token, c -> c.get("type", String.class)));
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String build(User user, long validity, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setClaims(Map.of(
                        "userId", user.getId(),
                        "role", user.getRole().name(),
                        "type", type
                ))
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(validity)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private boolean isExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
