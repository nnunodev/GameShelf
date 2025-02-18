package com.gameshelf.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

/**
 * Utility class for handling JWT (JSON Web Token) operations.
 * Provides functionality for generating, validating, and parsing JWTs used for authentication.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final long expirationTime;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtUtil(@Value("${jwt.expiration}") long expirationTime) {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.isEmpty()) {
            // Use a default secret for development only
            secret = "defaultSecretKeyForDevelopmentEnvironmentOnly123!@#";
            // Log a warning
            logger.warn("Using default JWT secret. Set JWT_SECRET environment variable in production!");

        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    /**
     * Generates a JWT token for the specified username.
     * 
     * @param username the username to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Generates a JWT token with role information.
     * 
     * @param username the username to include in the token
     * @param roles list of roles to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     * 
     * @param token the JWT token to parse
     * @return the username stored in the token
     * @throws JwtException if the token is invalid or expired
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error parsing JWT: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates a JWT token for a specific user.
     * 
     * @param token the JWT token to validate
     * @param username the username to verify against
     * @return true if the token is valid for the user, false otherwise
     */
    public boolean validateToken(String token, String username) {
        if (token == null || username == null || blacklistedTokens.contains(token)) {
            return false;
        }

        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Adds a token to the blacklist to prevent its future use.
     * 
     * @param token the JWT token to blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        // Remove expired tokens from blacklist periodically
        cleanupBlacklist();
    }

    private void cleanupBlacklist() {
        blacklistedTokens.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                return true;
            }
        });
    }

    /**
     * Extracts user roles from a JWT token.
     * 
     * @param token the JWT token to parse
     * @return list of role strings from the token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (List<String>) claims.get("roles");
        } catch (Exception e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String refreshToken(String oldToken) {
        String username = extractUsername(oldToken);
        return generateToken(username);
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
