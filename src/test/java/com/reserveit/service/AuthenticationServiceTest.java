package service;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.impl.AuthenticationService;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.model.Company;
import com.reserveit.util.JwtUtil;
import com.reserveit.util.PasswordHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserDatabase userDatabase;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService authService;


    @BeforeEach
    void setUp() {
        authService = new AuthenticationService(
                userDatabase,
                passwordHasher,
                jwtUtil,
                refreshTokenService,
                response
        );
        ReflectionTestUtils.setField(authService, "refreshTokenDuration", 604800000L);
    }

    @Nested
    class LoginTests {
        @Test
        void authenticate_SuccessfulLogin() {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("test@test.com", "password");
            User user = createSampleUser();
            RefreshToken refreshToken = createSampleRefreshToken(user);

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(user);
            when(passwordHasher.matches(request.getPassword(), user.getHashedPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(user)).thenReturn("test.access.token");
            when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

            // Act
            AuthenticationResponse response = authService.authenticate(request);

            // Assert
            assertNotNull(response);
            assertEquals("test.access.token", response.getAccessToken());
            verify(refreshTokenService).createRefreshToken(user);
        }

        @Test
        void authenticate_UserNotFound() {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("nonexistent@test.com", "password");
            when(userDatabase.findByEmail(request.getEmail())).thenReturn(null);

            // Act & Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> authService.authenticate(request));
        }

        @Test
        void authenticate_InvalidPassword() {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("test@test.com", "wrongpassword");
            User user = createSampleUser();

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(user);
            when(passwordHasher.matches(request.getPassword(), user.getHashedPassword())).thenReturn(false);

            // Act & Assert
            assertThrows(BadCredentialsException.class,
                    () -> authService.authenticate(request));
        }
        @Test
        void authenticate_WrongPassword_ThrowsException() {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("test@test.com", "wrongpass");
            User user = new User();
            user.setEmail("test@test.com");

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(user);
            when(passwordHasher.matches(request.getPassword(), user.getHashedPassword())).thenReturn(false);

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        }
    }

    @Nested
    class AuthenticateTests {
        @Test
        void authenticate_Success() {
            // Arrange
            AuthenticationRequest request = createAuthRequest();
            User user = createSampleUser();
            RefreshToken refreshToken = createSampleRefreshToken(user);

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(user);
            when(passwordHasher.matches(request.getPassword(), user.getHashedPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(user)).thenReturn("access_token");
            when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

            // Act
            AuthenticationResponse response = authService.authenticate(request);

            // Assert
            assertNotNull(response);
            assertEquals("access_token", response.getAccessToken());
            assertEquals("CUSTOMER", response.getRole());
            verify(userDatabase).findByEmail(request.getEmail());
            verifyCookieCreation();
        }

        @Test
        void authenticate_StaffUser_Success() {
            // Arrange
            AuthenticationRequest request = createAuthRequest();
            Staff staff = createSampleStaff();
            RefreshToken refreshToken = createSampleRefreshToken(staff);

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(staff);
            when(passwordHasher.matches(request.getPassword(), staff.getHashedPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(staff)).thenReturn("access_token");
            when(refreshTokenService.createRefreshToken(staff)).thenReturn(refreshToken);

            // Act
            AuthenticationResponse response = authService.authenticate(request);

            // Assert
            assertNotNull(response);
            assertEquals("access_token", response.getAccessToken());
            assertEquals("STAFF", response.getRole());
            assertNotNull(response.getCompanyId());
            verify(userDatabase).findByEmail(request.getEmail());
        }

        @Test
        void authenticate_UserNotFound() {
            // Arrange
            AuthenticationRequest request = createAuthRequest();
            when(userDatabase.findByEmail(request.getEmail())).thenReturn(null);

            // Act & Assert
            assertThrows(UsernameNotFoundException.class,
                    () -> authService.authenticate(request));
        }

        @Test
        void authenticate_InvalidPassword() {
            // Arrange
            AuthenticationRequest request = createAuthRequest();
            User user = createSampleUser();

            when(userDatabase.findByEmail(request.getEmail())).thenReturn(user);
            when(passwordHasher.matches(request.getPassword(), user.getHashedPassword())).thenReturn(false);

            // Act & Assert
            assertThrows(BadCredentialsException.class,
                    () -> authService.authenticate(request));
        }
    }

    @Nested
    class RegisterTests {
        @Test
        void register_Success() {
            // Arrange
            RegisterRequest request = createRegisterRequest();
            User user = createSampleUser();
            RefreshToken refreshToken = createSampleRefreshToken(user);

            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordHasher.hashPassword(request.getPassword())).thenReturn("hashed_password");
            when(userDatabase.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("access_token");
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

            // Act
            AuthenticationResponse response = authService.register(request);

            // Assert
            assertNotNull(response);
            assertEquals("access_token", response.getAccessToken());
            assertEquals("CUSTOMER", response.getRole());
            verify(userDatabase).save(any(User.class));
            verifyCookieCreation();
        }

        @Test
        void register_EmailExists() {
            // Arrange
            RegisterRequest request = createRegisterRequest();
            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> authService.register(request));
        }
        @Test
        void register_ExistingEmail_ThrowsException() {
            // Arrange
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");

            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.register(request));
            assertEquals("Email already exists", exception.getMessage());
        }
    }

    @Nested
    class RefreshTokenTests {

        @Test
        void refreshToken_RevokedToken_ThrowsException() {
            // Arrange
            String tokenStr = "revoked-token";
            RefreshToken revokedToken = createSampleRefreshToken(createSampleUser());
            revokedToken.setRevoked(true);

            when(refreshTokenService.findByToken(tokenStr))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(tokenStr));

            assertEquals("Refresh token not found", exception.getMessage());

            // Verify that clearAllAuthCookies is called which adds 4 cookies
            verify(response, times(4)).addCookie(any(Cookie.class));
        }

        @Test
        void refreshToken_TokenNotFound_ThrowsException() {
            // Arrange
            String tokenStr = "invalid-token";
            when(refreshTokenService.findByToken(tokenStr))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(tokenStr));

            assertEquals("Refresh token not found", exception.getMessage());

            // Verify that clearAllAuthCookies is called which adds 4 cookies
            verify(response, times(4)).addCookie(any(Cookie.class));
        }

        @Test
        void refreshToken_ExpiredToken_ThrowsException() {
            // Arrange
            String tokenStr = "expired-token";
            RefreshToken expiredToken = createSampleRefreshToken(createSampleUser());

            when(refreshTokenService.findByToken(tokenStr))
                    .thenReturn(Optional.of(expiredToken));
            when(refreshTokenService.verifyExpiration(expiredToken))
                    .thenThrow(new RuntimeException("Refresh token is expired or revoked. Please sign in again."));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(tokenStr));

            assertEquals("Refresh token is expired or revoked. Please sign in again.", exception.getMessage());

            // Verify that clearAllAuthCookies is called which adds 2 cookies (refreshToken and XSRF-TOKEN)
            verify(response, times(2)).addCookie(any(Cookie.class));
        }

        @Test
        void refreshToken_NullToken_ThrowsException() {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(null));

            assertEquals("No refresh token provided", exception.getMessage());

            // Verify that clearAllAuthCookies is called which adds 2 cookies (refreshToken and XSRF-TOKEN)
            verify(response, times(2)).addCookie(any(Cookie.class));
        }

        @Test
        void refreshToken_Success() {
            // Arrange
            String tokenStr = "valid-token";
            User user = createSampleUser();
            RefreshToken oldToken = createSampleRefreshToken(user);
            RefreshToken newToken = createSampleRefreshToken(user);
            String newAccessToken = "new.access.token";

            when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.of(oldToken));
            when(refreshTokenService.verifyExpiration(oldToken)).thenReturn(oldToken);
            when(jwtUtil.generateAccessToken(user)).thenReturn(newAccessToken);
            when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

            // Act
            AuthenticationResponse authResponse = authService.refreshToken(tokenStr);

            // Assert
            assertNotNull(authResponse);
            assertEquals(newAccessToken, authResponse.getAccessToken());

            // On success, we set one new refresh token cookie
            verify(response, times(1)).addCookie(any(Cookie.class));
        }
    }


    @Test
    void logout_Success() {
        // Arrange
        String refreshToken = "refresh_token";

        // Act
        authService.logout(refreshToken);

        // Assert
        verify(refreshTokenService).revokeRefreshToken(refreshToken);
        verify(response, atLeast(2)).addCookie(any(Cookie.class));
    }

    // Helper methods
    private AuthenticationRequest createAuthRequest() {
        return new AuthenticationRequest("test@example.com", "password");
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setPhoneNumber("1234567890");
        return request;
    }


    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setHashedPassword("hashed_password");
        user.setUserRole(UserRole.CUSTOMER);
        return user;
    }

    private Staff createSampleStaff() {
        Staff staff = new Staff(UserRole.STAFF);
        staff.setId(UUID.randomUUID());
        staff.setEmail("staff@example.com");
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setHashedPassword("hashed_password");
        Company company = new Company();
        company.setId(UUID.randomUUID());
        staff.setCompany(company);
        return staff;
    }

    private RefreshToken createSampleRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setUser(user);
        token.setToken("refresh_token");
        return token;
    }

    private void verifyCookieCreation() {
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("refreshToken", cookie.getName());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }
}