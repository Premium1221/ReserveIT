package com.reserveit.database.interfaces;

import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenDatabase {
    RefreshToken save(RefreshToken refreshToken); // Save a refresh token
    Optional<RefreshToken> findByToken(String token); // Find a refresh token by its token value
    Optional<RefreshToken> findByUser(User user); // Find refresh tokens for a specific user
    void delete(RefreshToken token); // Delete a specific refresh token
    void deleteAllByUser(User user); // Delete all refresh tokens for a specific user
    int deleteByExpiryDateLessThan(Instant date); // Delete expired tokens and return the count
    void revokeAllByUser(User user); // Revoke all refresh tokens for a specific user
}
