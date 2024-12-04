package service;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.impl.AuthenticationService;
import com.reserveit.logic.impl.EmailServiceImpl;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.RefreshToken;
import com.reserveit.model.User;
import com.reserveit.util.JwtUtil;
import com.reserveit.util.PasswordGenerator;
import com.reserveit.util.PasswordHasher;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserDatabase userDatabase;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private HttpServletResponse response;

    @Spy
    @InjectMocks
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthenticationService(
                userDatabase,
                passwordHasher,
                jwtUtil,
                emailService,
                passwordGenerator,
                refreshTokenService,
                response
        );

        ReflectionTestUtils.setField(authService, "refreshTokenDuration", 604800000L); // Set to 7 days
    }

    @Test
    void authenticate_ValidCredentials_ReturnsAuthenticationResponse() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setUserRole(UserRole.CUSTOMER);
        mockUser.setHashedPassword("hashedPassword");

        when(userDatabase.findByEmail(email)).thenReturn(mockUser);
        when(passwordHasher.matches(password, mockUser.getHashedPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(mockUser)).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(new RefreshToken());

        AuthenticationRequest request = new AuthenticationRequest(email, password);

        // Act
        AuthenticationResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals(UserRole.CUSTOMER.toString(), response.getRole());
    }

    @Test
    void authenticate_InvalidEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password";

        when(userDatabase.findByEmail(email)).thenReturn(null);

        AuthenticationRequest request = new AuthenticationRequest(email, password);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authService.authenticate(request));
    }

    @Test
    void authenticate_InvalidPassword_ThrowsBadCredentialsException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongPassword";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setHashedPassword("hashedPassword");

        when(userDatabase.findByEmail(email)).thenReturn(mockUser);
        when(passwordHasher.matches(password, mockUser.getHashedPassword())).thenReturn(false);

        AuthenticationRequest request = new AuthenticationRequest(email, password);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
    }

    @Test
    void register_NewUser_ReturnsAuthenticationResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("1234567890");

        when(userDatabase.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordHasher.hashPassword(request.getPassword())).thenReturn("hashedPassword");
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(new RefreshToken());

        // Act
        AuthenticationResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals(UserRole.CUSTOMER.toString(), response.getRole());
        verify(userDatabase, times(1)).save(any(User.class));
    }

    @Test
    void register_ExistingEmail_ThrowsRuntimeException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existinguser@example.com");

        when(userDatabase.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void refreshToken_ValidToken_ReturnsAuthenticationResponse() {
        // Arrange
        String refreshToken = "validRefreshToken";
        RefreshToken mockRefreshToken = new RefreshToken();
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setUserRole(UserRole.CUSTOMER);
        mockRefreshToken.setUser(mockUser);

        when(refreshTokenService.findByToken(refreshToken)).thenReturn(Optional.of(mockRefreshToken));
        when(refreshTokenService.verifyExpiration(mockRefreshToken)).thenReturn(mockRefreshToken);
        when(jwtUtil.generateAccessToken(mockUser)).thenReturn("newAccessToken");
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(new RefreshToken());

        // Act
        AuthenticationResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals(UserRole.CUSTOMER.toString(), response.getRole());
    }


}
