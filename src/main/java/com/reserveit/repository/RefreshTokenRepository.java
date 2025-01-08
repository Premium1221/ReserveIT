package com.reserveit.repository;

import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :expiryDate")
    int deleteByExpiryDateLessThan(Instant expiryDate); // Correct return type

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(User user); // Mark tokens as revoked
}
