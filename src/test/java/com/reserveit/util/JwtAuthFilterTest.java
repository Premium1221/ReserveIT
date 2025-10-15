package com.reserveit.util;

import com.reserveit.logic.interfaces.UserService;
import com.reserveit.model.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    private Environment environment = mock(Environment.class);

    @BeforeEach
    void setUp() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        jwtAuthFilter = new JwtAuthFilter(jwtUtil, userService, environment);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(email);
        when(userService.getUserEntityByEmail(email)).thenReturn(user);
        when(jwtUtil.validateAccessToken(token, user)).thenReturn(true);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userService).getUserEntityByEmail(email);
    }

    @Test
    void doFilterInternal_WithInvalidAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).getUserEntityByEmail(anyString());
    }

    @Test
    void doFilterInternal_WithNoAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).getUserEntityByEmail(anyString());
    }

    @Test
    void shouldNotFilter_PublicEndpoints() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        boolean result = jwtAuthFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }


    @Test
    void doFilterInternal_TokenValidationFails() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(email);
        when(userService.getUserEntityByEmail(email)).thenReturn(user);
        when(jwtUtil.validateAccessToken(token, user)).thenReturn(false);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userService).getUserEntityByEmail(email);
    }
}