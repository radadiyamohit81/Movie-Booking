package com.moviedb.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Resolves the currently authenticated user's numeric ID from the SecurityContext.
 *
 * WHY a separate class from SecurityUtils?
 *   User ID resolution requires a DB lookup (username → User entity). Keeping
 *   it separate avoids forcing SecurityUtils to depend on UserRepository,
 *   which would pull the JPA stack into a pure utility class.
 *
 * Usage in controllers:
 *   Long userId = AuthUtils.getCurrentUserId(userRepository);
 */
@UtilityClass
public class AuthUtils {

    /**
     * Extracts the username from the current SecurityContext.
     * Controllers/services use this to look up the User entity.
     *
     * @return authenticated username
     * @throws IllegalStateException if called with no active authentication
     */
    public String extractUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No active authentication in SecurityContext");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}
