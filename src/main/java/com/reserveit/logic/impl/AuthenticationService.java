package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.util.JwtUtil;
import com.reserveit.util.PasswordHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserDatabase userDatabase;
    private final PasswordHasher passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final HttpServletResponse response;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDuration;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userDatabase.findByEmail(request.getEmail());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        log.info("User attempting login: {}", user.getEmail());
        log.info("User role: {}", user.getUserRole());

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        setRefreshTokenCookie(refreshToken.getToken());

        String companyId = null;
        if (user instanceof Staff staff) {
            companyId = staff.getCompany().getId().toString();
        }

        // Remove "ROLE_" prefix for frontend consistency
        String role = user.getUserRole().toString().replace("ROLE_", "");
        return new AuthenticationResponse(accessToken, role, companyId);
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

        user = userDatabase.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        setRefreshTokenCookie(refreshToken.getToken());
     String companyId = null;
        return new AuthenticationResponse(accessToken, user.getUserRole().toString(), companyId);
    }


    public AuthenticationResponse refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            clearAllAuthCookies();
            throw new RuntimeException("No refresh token provided");
        }

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                    .orElseThrow(() -> {
                        clearAllAuthCookies();
                        return new RuntimeException("Refresh token not found");
                    });

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);
            User user = refreshToken.getUser();

            String accessToken = jwtUtil.generateAccessToken(user);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            setRefreshTokenCookie(newRefreshToken.getToken());

            String companyId = null;
            if (user instanceof Staff staff ) {
                companyId = staff.getCompany().getId().toString();
            }

            return new AuthenticationResponse(accessToken, user.getUserRole().toString(), companyId);
        } catch (Exception e) {
            clearAllAuthCookies();
            throw e;
        }
    }


    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        clearAllAuthCookies();
    }


    private void setRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenDuration / 1000));
        response.addCookie(cookie);
    }




    private void clearAllAuthCookies() {
        Cookie[] cookies = {
                createExpiredCookie("refreshToken", "/"),
                createExpiredCookie("XSRF-TOKEN", "/")
        };

        for (Cookie cookie : cookies) {
            response.addCookie(cookie);
        }
    }

    private Cookie createExpiredCookie(String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        return cookie;
    }
}
