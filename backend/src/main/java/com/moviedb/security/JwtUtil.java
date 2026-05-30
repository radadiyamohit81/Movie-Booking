package com.moviedb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Stateless JWT utility — generates and validates HS256 signed tokens.
 *
 * WHY HS256 (symmetric)?
 *   Both the token issuer (login) and verifier (filter) are the same service.
 *   Symmetric signing is simpler and faster for a single-service backend.
 *   Switch to RS256 (asymmetric) when a separate auth service issues tokens
 *   that resource servers need to verify independently.
 *
 * WHY Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))?
 *   The raw base64 secret from application.yml is decoded to bytes before
 *   key construction so the same secret string works predictably regardless
 *   of JVM locale or charset.
 *
 * Token claims:
 *   sub  → username (Spring Security principal name)
 *   role → single role string ("ROLE_USER" / "ROLE_ADMIN")
 *   iat  → issued-at timestamp
 *   exp  → expiry timestamp (24 h default)
 */
@Slf4j
@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey  = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    // ── Token generation ──────────────────────────────────────────────────

    public String generateToken(String username, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────

    /**
     * Returns true only when the token is:
     *   - cryptographically valid (correct signature)
     *   - not expired
     *   - structurally well-formed
     *
     * All exceptions are swallowed here; the caller (JwtFilter) treats
     * false as "unauthenticated" and lets Spring Security return 401.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── Claim extraction ──────────────────────────────────────────────────

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).getBody().get("role", String.class);
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}
