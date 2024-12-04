package com.reserveit.util;

import com.reserveit.logic.interfaces.UserService;
import com.reserveit.model.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip filtering for specific endpoints
        if (shouldNotFilter(request) || authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String accessToken = authHeader.substring(7);
            final String userEmail = jwtUtil.getEmailFromToken(accessToken);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserEntityByEmail(userEmail);

                if (jwtUtil.validateAccessToken(accessToken, user)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities() // Only ROLE-based authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Injected Authorities: " + authToken.getAuthorities());

                } else {
                    logger.warn("Invalid or expired access token for user: " + userEmail);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token is invalid or expired");
                    return;
                }
            }
        } catch (JwtException e) {
            logger.error("JWT token validation failed", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed: " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid token format", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
            return;
        } catch (Exception e) {
            logger.error("Authentication error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error occurred");
            return;
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
                "/api/companies",
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
