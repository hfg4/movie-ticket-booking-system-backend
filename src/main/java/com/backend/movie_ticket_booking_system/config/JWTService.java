package com.backend.movie_ticket_booking_system.config;

import com.backend.movie_ticket_booking_system.exceptions.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    private final JwtConfig jwtConfig;

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired");
            throw new InvalidJwtException("JWT token is expired", e);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature");
            throw new InvalidJwtException("Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format");
            throw new InvalidJwtException("Invalid JWT token format", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or null");
            throw new InvalidJwtException("JWT claims string is empty", e);
        } catch (Exception e) {
            log.error("Unexpected error while extracting username from token", e);
            throw new InvalidJwtException("Error extracting username from token: " + e.getMessage(), e);
        }
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;  // Token is expired
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

            if (isValid) {
                log.debug("JWT token validated successfully for user: {}", username);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }

            return isValid;
        } catch (InvalidJwtException e) {
            log.warn("JWT validation error: {}", e.getMessage());
            return false;
        }
    }


    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    @SuppressWarnings("unused")
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getJwtExpiration());

        try {
            String token = Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(jwtConfig.jwtSecretKey())
                    .compact();

            log.debug("JWT token generated successfully for user: {}", subject);
            return token;

        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", subject, e);
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}