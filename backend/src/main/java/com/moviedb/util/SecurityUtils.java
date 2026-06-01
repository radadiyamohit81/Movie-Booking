package com.moviedb.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Security utility — provides the authenticated user's identity from anywhere
 * in the application without repeating SecurityContextHolder boilerplate.
 *
 * WHY @UtilityClass (Lombok)?
 *   Makes the class final, adds a private constructor (preventing instantiation),
 *   and makes all methods static. A pure utility class with no state.
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Returns the username of the currently authenticated user.
     *
     * @throws IllegalStateException if called outside of an authenticated request
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in current context");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    /**
     * Returns true if the current user has the given role.
     * Role must be the full Spring Security format, e.g. "ROLE_ADMIN".
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    /** Convenience check for admin role. */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
