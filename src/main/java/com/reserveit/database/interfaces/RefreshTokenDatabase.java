package com.reserveit.database.interfaces;

import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;

import java.util.Optional;

public interface RefreshTokenDatabase {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
    void delete(RefreshToken token);
}