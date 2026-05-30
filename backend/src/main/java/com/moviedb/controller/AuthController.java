package com.moviedb.controller;

import com.moviedb.dto.AuthRequest;
import com.moviedb.dto.AuthResponse;
import com.moviedb.dto.RegisterRequest;
import com.moviedb.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public authentication endpoints — no JWT required.
 *
 * WHY @RequestMapping("/api/auth")?
 *   Groups all auth endpoints under a common path prefix that SecurityConfig
 *   permits without authentication: .requestMatchers("/api/auth/**").permitAll()
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
