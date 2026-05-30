package com.moviedb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration allowing the React dev server (port 3000) to communicate
 * with the Spring Boot API (port 8080).
 *
 * WHY explicit CORS config instead of @CrossOrigin on controllers?
 *   - @CrossOrigin on individual controllers is fragile — a new controller without
 *     the annotation silently breaks the frontend.
 *   - Centralised config here is the single source of truth. In production,
 *     replace "http://localhost:3000" with the deployed frontend URL.
 *
 * WHY allowCredentials(false)?
 *   We use Bearer tokens (not cookies) so credentials (cookies/auth headers
 *   sent by browser automatically) don't apply. Setting it to true with
 *   wildcard origins is a security misconfiguration (OWASP A05).
 *
 * Allowed methods:
 *   GET, POST, PUT, DELETE, OPTIONS
 *   OPTIONS is required for CORS pre-flight requests that browsers send
 *   before non-simple requests (POST with JSON body, DELETE, etc.).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the React dev server and any production frontend origin
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173"   // Vite dev server (if used)
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow Authorization header so Bearer tokens pass through
        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of("Authorization"));

        // Cache pre-flight response for 1 hour (reduces OPTIONS traffic)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
