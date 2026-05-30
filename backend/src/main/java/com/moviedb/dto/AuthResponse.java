package com.moviedb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body returned after successful login or registration.
 *
 * Contains only what the client strictly needs:
 *   - token   → stored in localStorage, sent as Authorization: Bearer <token>
 *   - username → displayed in the UI navbar
 *   - role    → controls which UI features are visible (admin panel etc.)
 *
 * WHY not return the full User entity?
 *   Password hash must NEVER be serialised to JSON. Returning a minimal DTO
 *   prevents accidental exposure even if the serialiser config changes.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
