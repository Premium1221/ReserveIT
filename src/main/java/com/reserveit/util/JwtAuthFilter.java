package com.reserveit.util;

import com.reserveit.logic.interfaces.UserService;
import com.reserveit.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;


@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final Environment environment;


    public JwtAuthFilter(JwtUtil jwtUtil, UserService userService, Environment environment) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.environment = environment;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Debug logging
        log.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        log.info("Auth Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No valid auth header found");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.getEmailFromToken(jwt);
            final String companyId = jwtUtil.getCompanyIdFromToken(jwt);

            log.info("JWT Token: {}...", jwt.substring(0, 10));
            log.info("User Email from Token: {}", userEmail);
            log.info("Company ID from Token: {}", companyId);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserEntityByEmail(userEmail);
                log.info("Found User: {} with role: {}", user.getEmail(), user.getUserRole());

                if (jwtUtil.validateAccessToken(jwt, user)) {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())
                    );

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

                    // Add additional details if needed
                    Map<String, String> details = new HashMap<>();
                    if (companyId != null) {
                        details.put("companyId", companyId);
                    }
                    authToken.setDetails(details);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set successfully. Authorities: {}", authorities);
                } else {
                    log.info("Token validation failed");
                }
            }
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("Checking if should filter path: {}", path);

        // List of paths to skip
        String[] skipPaths = {
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh",
                "/api/public",
                "/actuator/health",
        };

        boolean shouldSkip = Arrays.stream(skipPaths)
                .anyMatch(path::startsWith);

        // Skip filtering for test profile
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            log.info("Skipping filter for test profile");
            return true;
        }

        log.info("Should skip filtering: {}", Optional.of(shouldSkip));
        return shouldSkip;
    }
}