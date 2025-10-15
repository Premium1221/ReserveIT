package com.reserveit.config;

import com.reserveit.util.JwtAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_STAFF = "STAFF";

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/public/**",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/ws/**"
    };

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                        .requestMatchers("/api/tables/suggestions/**").permitAll()
                        .requestMatchers("/api/tables/restaurant/**").permitAll()
                        .requestMatchers("/api/tables/company/**").hasRole(ROLE_MANAGER)
                        .requestMatchers("/api/tables/**").hasAnyRole(ROLE_MANAGER, ROLE_ADMIN, ROLE_CUSTOMER)
                        .requestMatchers("/api/companies/{id}/dashboard").hasRole(ROLE_MANAGER)
                        .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/management/**").hasAnyRole(ROLE_MANAGER, ROLE_ADMIN)
                        .requestMatchers("/api/staff/**").hasAnyRole(ROLE_STAFF, ROLE_MANAGER, ROLE_ADMIN)
                        .requestMatchers("/api/reservations/**").hasAnyRole(ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER, ROLE_ADMIN)
                        .requestMatchers("api/reservations/all").hasAnyRole(ROLE_ADMIN)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5200",
                "http://127.0.0.1:5200",
                "http://172.29.96.1:5200"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(Boolean.valueOf(true));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
