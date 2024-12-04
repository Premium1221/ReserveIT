package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.RefreshTokenDatabase;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDuration;

    private final RefreshTokenDatabase refreshTokenDatabase;

    public RefreshTokenServiceImpl(RefreshTokenDatabase refreshTokenDatabase) {
        this.refreshTokenDatabase = refreshTokenDatabase;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        refreshTokenDatabase.findByUser(user)
                .ifPresent(refreshTokenDatabase::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));
        refreshToken.setRevoked(false);

        return refreshTokenDatabase.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0 || token.isRevoked()) {
            refreshTokenDatabase.delete(token);
            throw new RuntimeException("Refresh token was expired or revoked. Please make a new signin request");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenDatabase.findByToken(token);
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenDatabase.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenDatabase.save(refreshToken);
    }
}