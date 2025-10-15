package com.reserveit.util;

import com.reserveit.logic.interfaces.StaffService;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpiration;

    @Autowired
    private StaffService staffService;

    private Key getSignKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + user.getUserRole().name());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        if (user instanceof Staff staff) {
            UUID companyId = staff.getCompany().getId();
            log.info("Adding company ID to token: {}", companyId);
            claims.put("companyId", companyId.toString());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getCompanyIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("companyId", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateAccessToken(String token, User userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            boolean valid = claims.getSubject().equals(userDetails.getEmail()) &&
                    !isTokenExpired(claims.getExpiration());
            log.info("Token validation result for user {}: {}", userDetails.getEmail(), valid);
            return valid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
