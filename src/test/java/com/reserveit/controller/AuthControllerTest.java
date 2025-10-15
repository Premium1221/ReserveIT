package com.reserveit.controller;

import com.reserveit.controller.AuthController;
import com.reserveit.dto.AuthenticationRequest;
import com.reserveit.dto.AuthenticationResponse;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.logic.impl.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = createRegisterRequest();
        authRequest = createAuthRequest();
        authResponse = createAuthResponse();
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        Cookie refreshTokenCookie = new Cookie("refreshToken", "rtok");
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(authenticationService.refreshToken("rtok")).thenReturn(authResponse);

        // Act
        ResponseEntity<?> response = authController.refreshToken(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).refreshToken("rtok");
    }

    @Test
    void refreshToken_MissingCookie_Returns401() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        ResponseEntity<?> response = authController.refreshToken(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void register_Success() {
        // Arrange
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthenticationResponse> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse.getAccessToken(), response.getBody().getAccessToken());
        verify(authenticationService).register(registerRequest);
    }



    @Test
    void logout_Success() {
        // Arrange
        Cookie refreshTokenCookie = new Cookie("refreshToken", "valid-refresh-token");
        Cookie[] cookies = {refreshTokenCookie};
        when(request.getCookies()).thenReturn(cookies);
        doNothing().when(authenticationService).logout(anyString());

        // Act
        ResponseEntity<Void> response = authController.logout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).logout("valid-refresh-token");
    }

    @Test
    void logout_NoCookie() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        ResponseEntity<Void> response = authController.logout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).logout(null);
    }

    // Helper methods
    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("password123");
        request.setPhoneNumber("1234567890");
        return request;
    }

    private AuthenticationRequest createAuthRequest() {
        return new AuthenticationRequest("test@example.com", "password123");
    }

    private AuthenticationResponse createAuthResponse() {
        return new AuthenticationResponse("test.access.token", "CUSTOMER", null);
    }
}
