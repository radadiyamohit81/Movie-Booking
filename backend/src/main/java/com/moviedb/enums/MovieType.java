package com.moviedb.enums;

/**
 * Discriminates between a theatrical/streaming movie and a TV series.
 *
 * WHY an enum (not a plain String)?
 *   - Compile-time safety: typos in code caught at build time, not runtime.
 *   - @Enumerated(STRING) stores the human-readable name in the DB column
 *     so queries like WHERE type = 'movie' remain readable without a lookup table.
 *   - Adding a new type (e.g. SHORT) is a one-line change here, and the
 *     compiler immediately flags every switch/if that needs updating.
 */
public enum MovieType {
    movie,
    series
}
