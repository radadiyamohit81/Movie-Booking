package com.moviedb.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date/time formatting utilities used in DTO conversion and API responses.
 *
 * WHY centralise formatters?
 *   DateTimeFormatter is thread-safe and expensive to construct; creating one
 *   per invocation in service code wastes CPU. Static constants here are
 *   constructed once and reused everywhere.
 */
@UtilityClass
public class DateUtils {

    private static final DateTimeFormatter ISO_UTC =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Formats a LocalDateTime to ISO-8601 UTC string for API responses.
     * Example: 2024-05-31T15:57:21.000Z
     */
    public String toIsoString(LocalDateTime dt) {
        if (dt == null) return null;
        return ISO_UTC.format(dt);
    }

    /**
     * Formats a LocalDateTime to a human-readable display string.
     * Example: 31 May 2024
     */
    public String toDisplayDate(LocalDateTime dt) {
        if (dt == null) return null;
        return DISPLAY.format(dt);
    }

    /**
     * Returns how long ago a timestamp was, as a human-readable string.
     * Examples: "just now", "5 minutes ago", "3 hours ago", "2 days ago"
     *
     * Used in review/notification cards in the frontend.
     */
    public String timeAgo(LocalDateTime dt) {
        if (dt == null) return "";
        long seconds = java.time.Duration.between(dt, LocalDateTime.now()).getSeconds();

        if (seconds < 60)   return "just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";
        return toDisplayDate(dt);
    }
}
