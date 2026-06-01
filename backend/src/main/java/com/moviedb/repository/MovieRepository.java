package com.moviedb.repository;

import com.moviedb.model.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Movie data-access layer.
 *
 * WHY JpaSpecificationExecutor<Movie>?
 *   Enables findAll(Specification, Sort) — the foundation of the dynamic
 *   advanced search feature. MovieSpecifications composes predicates; this
 *   interface feeds them to Hibernate's CriteriaQuery engine.
 *   NOTE: The advanced search stays as Specification because JPQL does NOT
 *   support ":param IS NULL" for nullable collection parameters — that pattern
 *   throws a parse error in H2. Specifications return null (= no-op) safely.
 *
 * Simple named queries and @Query methods cover all other endpoints without
 * requiring the Specification overhead.
 */
@Repository
public interface MovieRepository extends
        JpaRepository<Movie, Long>,
        JpaSpecificationExecutor<Movie> {

    // ── Simple Spring Data named queries ──────────────────────────────────

    List<Movie> findByStatus(Movie.MovieStatus status);
    List<Movie> findByType(Movie.MovieType type);
    Optional<Movie> findByTitle(String title);
    List<Movie> findAllByOrderByCreatedAtDesc();
    List<Movie> findAllByOrderByPopularityDesc(Pageable pageable);

    // ── Named queries for home/browse endpoints ───────────────────────────

    /** Featured: top N by popularity (published only). */
    List<Movie> findByStatusOrderByPopularityDescRatingDesc(Movie.MovieStatus status);

    /** Trending: top by rating (published only). */
    List<Movie> findByStatusOrderByRatingDescPopularityDesc(Movie.MovieStatus status);

    /** Upcoming section on home page. */
    List<Movie> findByStatusOrderByCreatedAtDesc(Movie.MovieStatus status);

    /** /api/movies/type/{type} */
    List<Movie> findByTypeAndStatusOrderByPopularityDescRatingDesc(Movie.MovieType type, Movie.MovieStatus status);

    // ── Custom @Query methods ─────────────────────────────────────────────

    @Query("SELECT m FROM Movie m WHERE m.status = 'upcoming' ORDER BY m.createdAt DESC")
    List<Movie> findUpcomingOrderByCreatedAtDesc();

    @Query("SELECT m FROM Movie m WHERE m.status = 'published' ORDER BY m.popularity DESC")
    List<Movie> findFeaturedMovies(Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.type = :type AND m.status = :status")
    List<Movie> findByTypeAndStatus(
            @Param("type") Movie.MovieType type,
            @Param("status") Movie.MovieStatus status);

    // ── Basic Search (3 variants by type) ────────────────────────────────
    // These @Query variants are used by MovieService for the non-advanced path.
    // Advanced search (with nullable genre/year/rating filters) uses Specifications
    // in MovieSpecifications.java — JPQL cannot handle null collection params safely.

    /** type=title → case-insensitive title search */
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%',:query,'%')) AND m.status = :status")
    List<Movie> searchByTitleAndStatus(
            @Param("query") String query,
            @Param("status") Movie.MovieStatus status);

    /** type=celebs → search inside stars collection */
    @Query("SELECT m FROM Movie m WHERE m.status = :status AND EXISTS (SELECT 1 FROM m.stars s WHERE LOWER(s) LIKE LOWER(CONCAT('%',:query,'%')))")
    List<Movie> searchByStarsAndStatus(
            @Param("query") String query,
            @Param("status") Movie.MovieStatus status);

    /** type=all → search all text fields */
    @Query("""
            SELECT DISTINCT m FROM Movie m WHERE m.status = :status AND (
                LOWER(m.title)       LIKE LOWER(CONCAT('%',:query,'%')) OR
                LOWER(m.director)    LIKE LOWER(CONCAT('%',:query,'%')) OR
                LOWER(m.description) LIKE LOWER(CONCAT('%',:query,'%')) OR
                EXISTS (SELECT 1 FROM m.writers w WHERE LOWER(w) LIKE LOWER(CONCAT('%',:query,'%'))) OR
                EXISTS (SELECT 1 FROM m.stars   s WHERE LOWER(s) LIKE LOWER(CONCAT('%',:query,'%')))
            )""")
    List<Movie> searchByAllFieldsAndStatus(
            @Param("query") String query,
            @Param("status") Movie.MovieStatus status);
}

