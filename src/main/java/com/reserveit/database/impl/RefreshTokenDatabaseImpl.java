package com.reserveit.database.impl;

import com.reserveit.database.interfaces.RefreshTokenDatabase;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import com.reserveit.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

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
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public Optional<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }
}