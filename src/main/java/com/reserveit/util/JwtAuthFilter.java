package com.reserveit.util;

import com.reserveit.logic.interfaces.UserService;
import com.reserveit.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        // Debug print instead of logger
        System.out.println("Auth Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.getEmailFromToken(jwt);
            final String companyId = jwtUtil.getCompanyIdFromToken(jwt);

            // Debug prints
            System.out.println("User Email: " + userEmail);
            System.out.println("Company ID: " + companyId);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserEntityByEmail(userEmail);

                if (jwtUtil.validateAccessToken(jwt, user)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()))
                    );

                    if (companyId != null) {
                        Map<String, String> details = new HashMap<>();
                        details.put("companyId", companyId);
                        authToken.setDetails(details);
                    }

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set successfully for user: " + userEmail);
                }
            }
        } catch (Exception e) {
            // Simple error printing instead of logger
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String[] publicPaths = {
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh",
                "/api/public",
                "/actuator/health"
        };

        String path = request.getRequestURI();
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return false;
    }
}