package com.moviedb.config;

import com.moviedb.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * Design decisions:
 *
 * 1. STATELESS sessions
 *    No HttpSession is created or used — authentication state lives exclusively
 *    in the JWT. This enables horizontal scaling with zero sticky-session config.
 *
 * 2. CSRF disabled
 *    CSRF attacks exploit browser-managed session cookies. Since we use Bearer
 *    tokens (stored in localStorage, not sent automatically by the browser),
 *    CSRF is not a threat vector here. Enabling it would break all POST/DELETE
 *    calls from the React SPA without any security benefit.
 *
 * 3. Public vs protected endpoints
 *    - /api/auth/**    → login & register, no auth needed
 *    - /api/movies/**  → public read access (search, detail, featured)
 *    - /api/home/**    → home page data, public
 *    - /h2-console/**  → dev tool only (frameOptions relaxed for H2's iframe UI)
 *    - /api/admin/**   → ROLE_ADMIN only (enforced at SecurityFilterChain level
 *                        AND at method level via @PreAuthorize)
 *    - everything else → authenticated (any role)
 *
 * 4. BCryptPasswordEncoder
 *    Work factor 12 — higher than the default 10. Each additional factor doubles
 *    the hash time (~300ms at factor 12 on modern hardware), making offline
 *    brute-force attacks progressively more expensive.
 *
 * 5. JwtFilter registered before UsernamePasswordAuthenticationFilter
 *    Our filter extracts and validates the token; the standard filter is skipped
 *    (no form login), but its position in the chain is the conventional insertion
 *    point for pre-authentication filters.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on service/controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Disable form-login and basic auth (we use JWT only) ───────
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // ── Session management: stateless ────────────────────────────
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── H2 console needs frameOptions relaxed (it uses iframes) ──
            .headers(h ->
                h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // ── Endpoint authorisation rules ─────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public movie read endpoints
                .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/home/**").permitAll()
                // H2 dev console (disable in production by setting h2.console.enabled=false)
                .requestMatchers("/h2-console/**").permitAll()
                // Admin CRUD — ROLE_ADMIN only
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )

            // ── Plug our JWT filter into the chain ───────────────────────
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt with strength 12.
     * WHY @Bean? Allows injection into AuthService for password hashing
     * AND into DaoAuthenticationProvider for login verification — single instance,
     * no duplicate work-factor processing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
