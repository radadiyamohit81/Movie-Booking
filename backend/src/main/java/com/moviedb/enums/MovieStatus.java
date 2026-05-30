package com.moviedb.enums;

/**
 * Controls the content lifecycle / visibility of a Movie.
 *
 * States:
 *   published  — visible to end-users in all public search/browse endpoints.
 *   upcoming   — visible on the "upcoming" home section but not searchable.
 *   draft      — admin-only; hidden from all public APIs.
 *
 * WHY a lifecycle status instead of a boolean?
 *   A binary "isPublished" flag cannot express "coming soon" without a second
 *   boolean, creating impossible state combinations. An enum models the finite
 *   state machine correctly and is self-documenting.
 */
public enum MovieStatus {
    published,
    upcoming,
    draft
}
