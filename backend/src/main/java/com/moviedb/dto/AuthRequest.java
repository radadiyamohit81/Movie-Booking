package com.moviedb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/auth/login.
 *
 * WHY @NotBlank on both fields?
 *   Fail-fast at the controller layer before the service even touches the DB.
 *   Returns a structured 400 error rather than a cryptic NullPointerException.
 */
@Data
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
