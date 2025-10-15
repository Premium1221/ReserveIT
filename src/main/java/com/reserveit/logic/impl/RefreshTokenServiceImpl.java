package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.RefreshTokenDatabase;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDuration;

    private final RefreshTokenDatabase refreshTokenDatabase;

    public RefreshTokenServiceImpl(RefreshTokenDatabase refreshTokenDatabase) {
        this.refreshTokenDatabase = refreshTokenDatabase;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        try {
            // Revoke existing tokens instead of deleting them (for auditing purposes)
            refreshTokenDatabase.revokeAllByUser(user);

            // Create new refresh token
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));
            refreshToken.setRevoked(false);

            return refreshTokenDatabase.save(refreshToken);
        } catch (Exception e) {
            throw new RefreshTokenCreationException("Failed to create refresh token for user: " + user.getId(), e);
        }
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
            refreshTokenDatabase.delete(token);
            throw new RuntimeException("Refresh token is expired or revoked. Please sign in again.");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenDatabase.findByToken(token);
    }

    @Override
    public void revokeRefreshToken(String token) {
        try {
            refreshTokenDatabase.findByToken(token).ifPresent(refreshToken -> {
                refreshToken.setRevoked(true);
                refreshTokenDatabase.save(refreshToken);
            });
        } catch (Exception e) {
            log.error("Error revoking refresh token: {}", e.getMessage());
        }
    }
    @Override
    public void revokeAllUserTokens(User user) {
        try {
            refreshTokenDatabase.revokeAllByUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to revoke user tokens", e);
        }
    }
    @Override
    @Transactional
    public void deleteAllUserTokens(User user) {
        try {
            refreshTokenDatabase.deleteAllByUser(user); // Actually delete the tokens
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user tokens", e);
        }
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    @Profile("!test")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            int deletedTokens = refreshTokenDatabase.deleteByExpiryDateLessThan(Instant.now());
            log.info("Cleanup of expired tokens completed successfully. Deleted: {}", deletedTokens);
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }
    public static class RefreshTokenCreationException extends RuntimeException {
        public RefreshTokenCreationException(String message) {
            super(message);
        }

        public RefreshTokenCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
