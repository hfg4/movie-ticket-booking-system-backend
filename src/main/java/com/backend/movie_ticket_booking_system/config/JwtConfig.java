package com.backend.movie_ticket_booking_system.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:}")  // Default: 24 hours
    private long jwtExpiration;

    //Validates and creates a secure SecretKey for JWT signing
    //Requirements: Minimum 256 bits (32 bytes) for HS256

    //@return SecretKey for JWT operations
    //@throws IllegalArgumentException if jwtsecret is invalid or too short

    public SecretKey jwtSecretKey() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "Jwt is not configured"
            );
        }

        try {
            // First, try to decode as Base64
            byte[] decodedKey = Decoders.BASE64.decode(jwtSecret);

            // Validate minimum key length (256 bits = 32 bytes)
            if (decodedKey.length < 32) {
                throw new IllegalArgumentException();
            }

            System.out.println("JWT configuration validated successfully");
            return Keys.hmacShaKeyFor(decodedKey);

        } catch (IllegalArgumentException e) {
            // Not Base64 encoded, try as raw string
            if (e.getMessage() != null && e.getMessage().contains("Illegal base64 character")) {
                byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

                if (keyBytes.length < 32) {
                    throw new IllegalArgumentException();
                }

                System.out.println("JWT configuration validated successfully (raw string mode)");
                return Keys.hmacShaKeyFor(keyBytes);
            }

            throw new IllegalArgumentException(
                    "Failed to process JWT secret: " + e.getMessage()
            );
        }
    }
}