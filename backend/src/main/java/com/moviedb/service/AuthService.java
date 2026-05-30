package com.moviedb.service;

import com.moviedb.dto.AuthRequest;
import com.moviedb.dto.AuthResponse;
import com.moviedb.dto.RegisterRequest;
import com.moviedb.entity.User;
import com.moviedb.repository.UserRepository;
import com.moviedb.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login.
 *
 * WHY delegate authentication to AuthenticationManager?
 *   AuthenticationManager.authenticate() invokes the full Spring Security chain:
 *   DaoAuthenticationProvider → UserDetailsService → BCrypt comparison.
 *   We don't re-implement that logic — we reuse the tested, secure implementation.
 *   If authentication fails (bad password, user not found), Spring Security throws
 *   BadCredentialsException which GlobalExceptionHandler maps to HTTP 401.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Input validation: unique constraints
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                // WHY hash here and not in entity @PrePersist?
                // Password hashing is an application-layer concern; the entity
                // should not know about BCrypt. Keeping it in the service makes
                // it explicit and testable.
                .password(passwordEncoder.encode(req.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(AuthRequest req) {
        // Delegate to Spring Security — throws BadCredentialsException on failure
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // At this point authentication succeeded; fetch role from DB
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
