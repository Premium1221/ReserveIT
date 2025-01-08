package com.reserveit.database.impl;

import com.reserveit.database.interfaces.RefreshTokenDatabase;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import com.reserveit.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class RefreshTokenDatabaseImpl implements RefreshTokenDatabase {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenDatabaseImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        System.out.println("Looking up refresh token: " + token);
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public Optional<RefreshToken> findByUser(User user) {
        System.out.println("Looking up refresh tokens for user: " + user.getEmail());
        return refreshTokenRepository.findByUser(user);
    }

    @Override
    public void delete(RefreshToken token) {
        System.out.println("Deleting refresh token: " + token.getToken());
        refreshTokenRepository.delete(token);
    }

    @Override
    public void deleteAllByUser(User user) {
        System.out.println("Deleting all refresh tokens for user: " + user.getEmail());
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public int deleteByExpiryDateLessThan(Instant date) {
        System.out.println("Deleting expired refresh tokens older than: " + date);
        return refreshTokenRepository.deleteByExpiryDateLessThan(date);
    }

    @Override
    public void revokeAllByUser(User user) {
        System.out.println("Revoking all refresh tokens for user: " + user.getEmail());
        refreshTokenRepository.revokeAllByUser(user);
    }

}
