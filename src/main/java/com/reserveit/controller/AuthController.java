package com.reserveit.controller;

import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.logic.impl.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5200", "http://127.0.0.1:5200", "http://172.29.96.1:5200"},
        allowCredentials = "true"
)
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token is missing"));
        }

        try {
            AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        authenticationService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
