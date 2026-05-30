package com.moviedb.service;

import com.moviedb.dto.ReviewRequest;
import com.moviedb.entity.Review;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository  movieRepository;

    @Transactional
    public Review createReview(Long userId, ReviewRequest req) {
        if (!movieRepository.existsById(req.getMovieId())) {
            throw new ResourceNotFoundException("Movie", req.getMovieId());
        }

        Review review = Review.builder()
                .userId(userId)
                .movieId(req.getMovieId())
                .content(req.getContent())
                .spoiler(req.isSpoiler())
                .status("published")
                .build();

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForMovie(Long movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }
        reviewRepository.deleteById(reviewId);
    }
}
