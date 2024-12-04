package com.reserveit.logic.interfaces;

import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import java.util.Optional;


public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyExpiration(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);  // Make sure it returns Optional<RefreshToken>
    void revokeRefreshToken(String token);
}