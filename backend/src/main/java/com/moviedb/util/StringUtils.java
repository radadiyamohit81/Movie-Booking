package com.moviedb.util;

import lombok.experimental.UtilityClass;

/**
 * String utility methods used across services.
 *
 * WHY not use Apache Commons Lang StringUtils?
 *   The project only needs a few specific helpers. Pulling in a full utility
 *   library for three methods adds unnecessary dependency weight.
 */
@UtilityClass
public class StringUtils {

    /**
     * Returns true if the string is null, empty, or contains only whitespace.
     * Mirrors Spring's StringUtils.hasText() logic as a null-safe complement.
     */
    public boolean isBlankOrNull(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Truncates a string to maxLength characters and appends "..." if truncated.
     * Useful for notification messages and preview snippets.
     *
     * @param s         the input string (null-safe)
     * @param maxLength maximum allowed length before truncation
     * @return the original string if within limit, or a truncated version
     */
    public String truncate(String s, int maxLength) {
        if (s == null) return "";
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 3) + "...";
    }

    /**
     * Converts a raw search query to a safe LIKE pattern.
     * Escapes SQL wildcards in user input to prevent pattern injection.
     *
     * @param query the raw user-provided search string
     * @return a lower-cased LIKE pattern: %query%
     */
    public String toLikePattern(String query) {
        if (isBlankOrNull(query)) return "%";
        // Escape literal % and _ so they're treated as characters, not wildcards
        String escaped = query
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped.toLowerCase() + "%";
    }
}
