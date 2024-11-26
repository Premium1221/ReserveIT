package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.IUserDatabase;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.UserRole;
import com.reserveit.model.User;
import com.reserveit.util.JwtUtil;
import com.reserveit.util.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final IUserDatabase userDatabase;
    private final PasswordHasher passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userDatabase.findByEmail(request.getEmail());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String role = user.getUserRole().toString();

        return new AuthenticationResponse(accessToken, refreshToken, role);
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
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String role = user.getUserRole().toString();

        return new AuthenticationResponse(accessToken, refreshToken, role);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = userDatabase.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        String role = user.getUserRole().toString();

        return new AuthenticationResponse(newAccessToken, newRefreshToken, role);
    }

    public void logout(String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }
}