package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.User;
import com.reserveit.model.RefreshToken;
import com.reserveit.util.JwtUtil;
import com.reserveit.util.PasswordGenerator;
import com.reserveit.util.PasswordHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserDatabase userDatabase;
    private final PasswordHasher passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailServiceImpl emailService;
    private final PasswordGenerator passwordGenerator;
    private final RefreshTokenService refreshTokenService;
    private final HttpServletResponse response;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDuration;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userDatabase.findByEmail(request.getEmail());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Set refresh token in HTTP-only cookie
        addRefreshTokenCookie(refreshToken.getToken());

        return new AuthenticationResponse(accessToken, user.getUserRole().toString());
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (userDatabase.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setHashedPassword(passwordEncoder.hashPassword(request.getPassword()));
        user.setUserRole(UserRole.CUSTOMER);

        userDatabase.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Set refresh token in HTTP-only cookie
        addRefreshTokenCookie(refreshToken.getToken());

        return new AuthenticationResponse(accessToken, user.getUserRole().toString());
    }

    public AuthenticationResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.verifyExpiration(
                refreshTokenService.findByToken(refreshTokenStr)
                        .orElseThrow(() -> new RuntimeException("Refresh token not found"))
        );

        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateAccessToken(user);

        // Generate new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        addRefreshTokenCookie(newRefreshToken.getToken());

        return new AuthenticationResponse(accessToken, user.getUserRole().toString());
    }

    public void logout() {
        // Clear the refresh token cookie
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void createUserByAdmin(RegisterRequest request) {
        if (userDatabase.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String generatedPassword = passwordGenerator.generateSecurePassword();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setHashedPassword(passwordEncoder.hashPassword(generatedPassword));

        try {
            user.setUserRole(request.getRole() != null ?
                    UserRole.valueOf(request.getRole().toUpperCase()) :
                    UserRole.CUSTOMER);
        } catch (IllegalArgumentException e) {
            user.setUserRole(UserRole.CUSTOMER);
        }

        userDatabase.save(user);

        emailService.sendUserCredentials(
                request.getEmail(),
                user.getFullName(),
                generatedPassword
        );
    }

    private void addRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true); // Makes cookie inaccessible to JavaScript
        cookie.setSecure(true);   // Only sent over HTTPS
        cookie.setPath("/api");   // Cookie path
        cookie.setMaxAge(refreshTokenDuration.intValue() / 1000); // Convert ms to seconds
        response.addCookie(cookie);
    }
}