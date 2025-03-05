package com.example.progrest.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // ✅ Hardcoded Secret Key (Must be at least 32 characters)
    private static final String SECRET_KEY = "this_is_a_very_secure_secret_key_32_chars!";

    // ✅ Hardcoded Expiration Times (Milliseconds)
    private static final long ACCESS_EXPIRATION = 60 * 60 * 1000L;  // 1 Hour
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;  // 7 Days

    // ✅ Generate Signing Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Generate Access Token
    public String generateAccessToken(String email) {
        return generateToken(email, ACCESS_EXPIRATION, "access");
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken(String email) {
        return generateToken(email, REFRESH_EXPIRATION, "refresh");
    }

    // ✅ General Token Generator
   // ✅ General Token Generator (Add email claim)
private String generateToken(String email, long expirationTime, String type) {
  return Jwts.builder()
          .setSubject(email)
          .claim("email", email)  // ✅ Add email to payload
          .claim("type", type)
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
          .signWith(getSigningKey(), SignatureAlgorithm.HS256)
          .compact();
}


    // ✅ Extract Email from Token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ Check if Token is Expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ✅ Check if Token is Access Token
    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    // ✅ Check if Token is Refresh Token
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    // ✅ Validate Access Token
    public boolean validateToken(String token, String email) {
        try {
            return extractEmail(token).equals(email) && !isTokenExpired(token) && isAccessToken(token);
        } catch (Exception e) {
            return false; // 🔹 Prevent token leak
        }
    }

    // ✅ Validate Refresh Token
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().after(new Date()) && isRefreshToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Extract Expiration Date from Token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ Extract Specific Claim from Token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
