package com.nexusmarket.infrastructure.security;

import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String VERSION_CLAIM = "ver";
    private static final int MIN_SECRET_BYTES = 32;

    private final String secret;
    private final long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secret = validateSecret(secret);
        if (expiration <= 0) {
            throw new IllegalStateException("jwt.expiration must be greater than zero");
        }
        this.expiration = expiration;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList());

        // tokenVersion del usuario para invalidación de sesiones
        Integer tokenVersion = 0;
        if (userDetails instanceof UserDetailsAdapter adapter) {
            tokenVersion = adapter.getUser().getTokenVersion();
            if (tokenVersion == null) {
                tokenVersion = 0;
            }
        }
        claims.put(VERSION_CLAIM, tokenVersion);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString()) // jti: identificador único del token
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Integer extractVersion(String token) {
        return extractClaim(token, claims -> claims.get(VERSION_CLAIM, Integer.class));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final Integer tokenVersion = extractVersion(token);

            Integer userVersion = 0;
            if (userDetails instanceof UserDetailsAdapter adapter) {
                userVersion = adapter.getUser().getTokenVersion();
                if (userVersion == null) {
                    userVersion = 0;
                }
            }

            // La versión del token debe coincidir con la versión actual del usuario
            if (!Objects.equals(tokenVersion, userVersion)) {
                return false;
            }

            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static String validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        return secret;
    }
}
