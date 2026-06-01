package com.moviedb.repository;

import com.moviedb.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByUserId(Long userId);

    /** Used by ContentModerationService for bulk scan. */
    List<Review> findByStatus(String status);
}
