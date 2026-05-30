package com.moviedb.repository;

import com.moviedb.entity.Movie;
import com.moviedb.enums.MovieStatus;
import com.moviedb.enums.MovieType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Movie data-access layer.
 *
 * WHY JpaSpecificationExecutor<Movie>?
 *   Enables findAll(Specification, Sort) — the foundation of the dynamic
 *   search feature. MovieSpecifications composes predicates; this interface
 *   feeds them to Hibernate's CriteriaQuery engine.
 *
 * Simple named queries cover the non-search endpoints (featured, upcoming,
 * by type) without any JPQL boilerplate.
 */
@Repository
public interface MovieRepository extends
        JpaRepository<Movie, Long>,
        JpaSpecificationExecutor<Movie> {

    /** Featured: top N by popularity (published only). */
    List<Movie> findByStatusOrderByPopularityDescRatingDesc(MovieStatus status);

    /** Trending: top by rating (published only). */
    List<Movie> findByStatusOrderByRatingDescPopularityDesc(MovieStatus status);

    /** Upcoming section on home page. */
    List<Movie> findByStatusOrderByCreatedAtDesc(MovieStatus status);

    /** /api/movies/type/{type} */
    List<Movie> findByTypeAndStatusOrderByPopularityDescRatingDesc(MovieType type, MovieStatus status);
}
