package com.moviedb.repository.spec;

import com.moviedb.model.Movie;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Composable JPA Specifications for dynamic Movie search.
 *
 * WHY Specifications instead of a single giant @Query?
 *   The spec approach (GoF Specification pattern) solves two problems:
 *
 *   1. NULL parameters in JPQL collection checks.
 *      JPQL does not support ":param IS NULL" for collection-type parameters.
 *      Each Specification returns null when its filter is inactive; Spring Data
 *      JPA Specification.where(null).and(null) safely produces "no predicate"
 *      (i.e. no WHERE clause added for that filter).
 *
 *   2. Composability / Open-Closed Principle.
 *      Adding a new filter (e.g. "language") = one new static method here,
 *      one .and(…) call in MovieService. No existing query string is touched.
 *
 * WHY correlated EXISTS subqueries for @ElementCollection fields?
 *   Element collections (writers, stars, genre) live in separate tables.
 *   A direct JOIN on those tables multiplies rows, requiring DISTINCT and
 *   causing COUNT/pagination errors. EXISTS subqueries are non-multiplying
 *   and produce exactly one result row per Movie regardless of list size.
 *
 * Convention: every method returns null when its parameter is null/blank,
 * meaning "no filter applied" — Specification.where(null) is a no-op.
 */
public final class MovieSpecifications {

    private MovieSpecifications() {}

    // ── Base filter ───────────────────────────────────────────────────────

    public static Specification<Movie> isPublished() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), Movie.MovieStatus.PUBLISHED);
    }

    public static Specification<Movie> isUpcoming() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), Movie.MovieStatus.UPCOMING);
    }

    // ── Text query ────────────────────────────────────────────────────────

    /**
     * type=all → search title, director, description, writers list, stars list.
     * All comparisons use LOWER() on both sides for true case-insensitivity.
     */
    public static Specification<Movie> hasTextQueryAll(String q) {
        if (q == null || q.isBlank()) return null;
        final String pattern = "%" + q.toLowerCase() + "%";

        return (root, query, cb) -> {
            query.distinct(true);

            Predicate titleMatch   = cb.like(cb.lower(root.get("title")),       pattern);
            Predicate dirMatch     = cb.like(cb.lower(root.get("director")),    pattern);
            Predicate descMatch    = cb.like(cb.lower(root.get("description")), pattern);
            Predicate writerExists = buildCollectionExistsPredicate(root, query, cb, "writers", pattern);
            Predicate starExists   = buildCollectionExistsPredicate(root, query, cb, "stars",   pattern);

            return cb.or(titleMatch, dirMatch, descMatch, writerExists, starExists);
        };
    }

    /** type=title → search only by title. */
    public static Specification<Movie> hasTextQueryTitle(String q) {
        if (q == null || q.isBlank()) return null;
        final String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), pattern);
    }

    /** type=celebs → search inside stars list only. */
    public static Specification<Movie> hasTextQueryCelebs(String q) {
        if (q == null || q.isBlank()) return null;
        final String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            return buildCollectionExistsPredicate(root, query, cb, "stars", pattern);
        };
    }

    // ── Numeric / date filters ────────────────────────────────────────────

    public static Specification<Movie> hasMinRating(Double minRating) {
        if (minRating == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("rating"), minRating);
    }

    public static Specification<Movie> hasMaxRating(Double maxRating) {
        if (maxRating == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("rating"), maxRating);
    }

    public static Specification<Movie> hasYear(Integer year) {
        if (year == null) return null;
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    public static Specification<Movie> hasMinYear(Integer minYear) {
        if (minYear == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("year"), minYear);
    }

    public static Specification<Movie> hasMaxYear(Integer maxYear) {
        if (maxYear == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("year"), maxYear);
    }

    // ── String / collection filters ───────────────────────────────────────

    public static Specification<Movie> hasDirector(String director) {
        if (director == null || director.isBlank()) return null;
        final String pattern = "%" + director.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("director")), pattern);
    }

    public static Specification<Movie> hasWriter(String writer) {
        if (writer == null || writer.isBlank()) return null;
        final String pattern = "%" + writer.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            return buildCollectionExistsPredicate(root, query, cb, "writers", pattern);
        };
    }

    /**
     * Genre filter uses OR logic: match any one genre in the provided list.
     * e.g. genre=Action,Drama → movies that have Action OR Drama in their genre list.
     */
    public static Specification<Movie> hasGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            Subquery<Integer> sq = query.subquery(Integer.class);
            Root<Movie> sqRoot   = sq.from(Movie.class);
            Join<Movie, String> join = sqRoot.join("genre", JoinType.INNER);
            sq.select(cb.literal(1))
              .where(
                  cb.equal(sqRoot.get("id"), root.get("id")),
                  join.in(genres)
              );
            return cb.exists(sq);
        };
    }

    // ── Internal helper ───────────────────────────────────────────────────

    /**
     * Builds a correlated EXISTS subquery for LIKE matching inside an
     * @ElementCollection field (writers, stars).
     *
     *   EXISTS (
     *       SELECT 1 FROM Movie m2 JOIN m2.<field> elem
     *       WHERE m2.id = outer.id AND LOWER(elem) LIKE :pattern
     *   )
     */
    private static Predicate buildCollectionExistsPredicate(
            Root<Movie> outerRoot,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String collectionField,
            String likePattern) {

        Subquery<Integer> sq    = query.subquery(Integer.class);
        Root<Movie> sqRoot      = sq.from(Movie.class);
        Join<Movie, String> join = sqRoot.join(collectionField, JoinType.INNER);

        sq.select(cb.literal(1))
          .where(
              cb.equal(sqRoot.get("id"), outerRoot.get("id")),
              cb.like(cb.lower(join), likePattern)
          );
        return cb.exists(sq);
    }
}
