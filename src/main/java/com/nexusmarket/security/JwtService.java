package com.nexusmarket.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and validates the JSON Web Tokens used by the NexusMarket REST API.
 *
 * <p>
 * Tokens are signed with HMAC-SHA256 using the key configured through
 * {@code jwt.secret}. They carry the user email in the {@code sub} claim and
 * the granted authorities in a custom {@code roles} claim, so the API does not
 * need a database round trip on every request.</p>
 */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final String secret;
    private final long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * Builds a signed token for the given authenticated principal.
     *
     * @param userDetails the principal whose email and authorities are embedded
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the {@code sub} claim, which holds the user email.
     *
     * @param token the JWT to inspect
     * @return the email stored in the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Verifies that the token belongs to the given principal and has not
     * expired.
     *
     * @param token the JWT received in the Authorization header
     * @param userDetails the principal loaded from the database
     * @return {@code true} when the subject matches and the token is still
     * valid
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            // Signature mismatch, malformed token, expired token, empty claims...
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

    /**
     * Derives the HMAC signing key from the configured secret.
     *
     * <p>
     * The secret is used as raw UTF-8 bytes, which lets you keep a plain
     * alphanumeric string in the configuration. {@code Keys.hmacShaKeyFor}
     * rejects anything shorter than 256 bits with a {@code WeakKeyException},
     * so a too-short {@code jwt.secret} fails fast at startup instead of
     * silently weakening security.</p>
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
