package com.reserveit.database.impl;

import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import com.reserveit.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenDatabaseImplTest {

    @Mock
    private RefreshTokenRepository repository;

    @InjectMocks
    private RefreshTokenDatabaseImpl db;

    @Test
    void delegates_all_crud_and_queries(){
        RefreshToken token = new RefreshToken();
        token.setToken("abc");
        User user = new User();
        user.setEmail("u@test.com");

        when(repository.save(any(RefreshToken.class))).thenReturn(token);
        when(repository.findByToken("abc")).thenReturn(Optional.of(token));
        when(repository.findByUser(user)).thenReturn(Optional.of(token));
        when(repository.deleteByExpiryDateLessThan(any(Instant.class))).thenReturn(3);

        assertNotNull(db.save(token));
        assertTrue(db.findByToken("abc").isPresent());
        assertTrue(db.findByUser(user).isPresent());
        assertEquals(3, db.deleteByExpiryDateLessThan(Instant.now()));

        db.delete(token);
        verify(repository).delete(token);
        db.deleteAllByUser(user);
        verify(repository).deleteByUser(user);
        db.revokeAllByUser(user);
        verify(repository).revokeAllByUser(user);
    }
}

