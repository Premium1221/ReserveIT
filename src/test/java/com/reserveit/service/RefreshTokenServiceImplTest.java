package service;

import com.reserveit.database.interfaces.RefreshTokenDatabase;
import com.reserveit.logic.impl.RefreshTokenServiceImpl;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenDatabase refreshTokenDatabase;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        // Set the refresh token duration (24 hours in milliseconds)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDuration", 86400000L);
    }

    @Nested
    class CreateRefreshTokenTests {
        @Test
        void createRefreshToken_Success() {
            // Arrange
            User user = createSampleUser();
            RefreshToken token = createSampleToken(user);

            when(refreshTokenDatabase.save(any(RefreshToken.class))).thenReturn(token);
            doNothing().when(refreshTokenDatabase).revokeAllByUser(user);

            // Act
            RefreshToken result = refreshTokenService.createRefreshToken(user);

            // Assert
            assertNotNull(result);
            assertEquals(user, result.getUser());
            assertFalse(result.isRevoked());
            verify(refreshTokenDatabase).revokeAllByUser(user);
            verify(refreshTokenDatabase).save(any(RefreshToken.class));
        }

        @Test
        void createRefreshToken_DatabaseError() {
            // Arrange
            User user = createSampleUser();
            when(refreshTokenDatabase.save(any(RefreshToken.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> refreshTokenService.createRefreshToken(user));
        }
    }

    @Nested
    class VerifyExpirationTests {
        @Test
        void verifyExpiration_ValidToken() {
            // Arrange
            RefreshToken token = createSampleToken(createSampleUser());
            token.setExpiryDate(Instant.now().plusSeconds(3600)); // Token expires in 1 hour
            token.setRevoked(false);

            // Act
            RefreshToken result = refreshTokenService.verifyExpiration(token);

            // Assert
            assertNotNull(result);
            assertEquals(token, result);
            verify(refreshTokenDatabase, never()).delete(any(RefreshToken.class));
        }

        @Test
        void verifyExpiration_ExpiredToken() {
            // Arrange
            RefreshToken token = createSampleToken(createSampleUser());
            token.setExpiryDate(Instant.now().minusSeconds(3600)); // Token expired 1 hour ago

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> refreshTokenService.verifyExpiration(token));
            verify(refreshTokenDatabase).delete(token);
        }

        @Test
        void verifyExpiration_RevokedToken() {
            // Arrange
            RefreshToken token = createSampleToken(createSampleUser());
            token.setExpiryDate(Instant.now().plusSeconds(3600));
            token.setRevoked(true);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> refreshTokenService.verifyExpiration(token));
            verify(refreshTokenDatabase).delete(token);
        }
    }

    @Nested
    class FindByTokenTests {
        @Test
        void findByToken_ExistingToken() {
            // Arrange
            String tokenString = "valid-token";
            RefreshToken token = createSampleToken(createSampleUser());
            when(refreshTokenDatabase.findByToken(tokenString))
                    .thenReturn(Optional.of(token));

            // Act
            Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(token, result.get());
        }

        @Test
        void findByToken_NonExistentToken() {
            // Arrange
            String tokenString = "non-existent-token";
            when(refreshTokenDatabase.findByToken(tokenString))
                    .thenReturn(Optional.empty());

            // Act
            Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class RevokeTokenTests {
        @Test
        void revokeRefreshToken_Success() {
            // Arrange
            String tokenString = "valid-token";
            RefreshToken token = createSampleToken(createSampleUser());
            when(refreshTokenDatabase.findByToken(tokenString))
                    .thenReturn(Optional.of(token));
            when(refreshTokenDatabase.save(any(RefreshToken.class)))
                    .thenReturn(token);

            // Act
            refreshTokenService.revokeRefreshToken(tokenString);

            // Assert
            verify(refreshTokenDatabase).findByToken(tokenString);
            verify(refreshTokenDatabase).save(token);
            assertTrue(token.isRevoked());
        }

        @Test
        void revokeRefreshToken_NonExistentToken() {
            // Arrange
            String tokenString = "non-existent-token";
            when(refreshTokenDatabase.findByToken(tokenString))
                    .thenReturn(Optional.empty());

            // Act
            refreshTokenService.revokeRefreshToken(tokenString);

            // Assert
            verify(refreshTokenDatabase).findByToken(tokenString);
            verify(refreshTokenDatabase, never()).save(any(RefreshToken.class));
        }
    }

    @Nested
    class TokenManagementTests {
        @Test
        void revokeAllUserTokens_Success() {
            // Arrange
            User user = createSampleUser();
            doNothing().when(refreshTokenDatabase).revokeAllByUser(user);

            // Act
            refreshTokenService.revokeAllUserTokens(user);

            // Assert
            verify(refreshTokenDatabase).revokeAllByUser(user);
        }

        @Test
        void revokeAllUserTokens_DatabaseError() {
            // Arrange
            User user = createSampleUser();
            doThrow(new RuntimeException("Database error"))
                    .when(refreshTokenDatabase).revokeAllByUser(user);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> refreshTokenService.revokeAllUserTokens(user));
        }

        @Test
        void deleteAllUserTokens_Success() {
            // Arrange
            User user = createSampleUser();
            doNothing().when(refreshTokenDatabase).deleteAllByUser(user);

            // Act
            refreshTokenService.deleteAllUserTokens(user);

            // Assert
            verify(refreshTokenDatabase).deleteAllByUser(user);
        }

        @Test
        void deleteAllUserTokens_DatabaseError() {
            // Arrange
            User user = createSampleUser();
            doThrow(new RuntimeException("Database error"))
                    .when(refreshTokenDatabase).deleteAllByUser(user);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> refreshTokenService.deleteAllUserTokens(user));
        }
    }

    @Test
    void cleanupExpiredTokens_Success() {
        // Arrange
        when(refreshTokenDatabase.deleteByExpiryDateLessThan(any(Instant.class)))
                .thenReturn(5);

        // Act
        refreshTokenService.cleanupExpiredTokens();

        // Assert
        verify(refreshTokenDatabase).deleteByExpiryDateLessThan(any(Instant.class));
    }

    @Test
    void cleanupExpiredTokens_HandlesError() {
        // Arrange
        when(refreshTokenDatabase.deleteByExpiryDateLessThan(any(Instant.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertDoesNotThrow(() -> refreshTokenService.cleanupExpiredTokens());
    }

    // Helper methods
    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private RefreshToken createSampleToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(86400000L));
        token.setRevoked(false);
        return token;
    }
}