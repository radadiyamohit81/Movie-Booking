package com.moviedb.service;

import com.moviedb.model.Review;
import com.moviedb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Content moderation service — flags or removes inappropriate reviews.
 *
 * WHY a dedicated service instead of embedding moderation in ReviewService?
 *   Single Responsibility: ReviewService handles the CRUD lifecycle of reviews.
 *   ContentModerationService handles the moderation lifecycle independently.
 *   This keeps both services small and testable in isolation.
 *
 * Current implementation: keyword-based flag detection.
 * Future: plug in an external moderation API (e.g. OpenAI Moderation, AWS Rekognition).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final ReviewRepository reviewRepository;

    /** Configurable set of prohibited terms. Extend or load from DB/config. */
    private static final Set<String> PROHIBITED_TERMS = Set.of(
            "spam", "scam", "fake", "hate"
    );

    /**
     * Scans review content and flags it if prohibited terms are found.
     * Sets status to "flagged" so admins can review and decide.
     *
     * @param reviewId the ID of the review to moderate
     * @return true if the review was flagged, false if it passed moderation
     */
    @Transactional
    public boolean moderateReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        String contentLower = review.getContent().toLowerCase();
        boolean hasFlaggedContent = PROHIBITED_TERMS.stream()
                .anyMatch(contentLower::contains);

        if (hasFlaggedContent) {
            review.setStatus("flagged");
            reviewRepository.save(review);
            log.warn("Review {} flagged for prohibited content", reviewId);
        }

        return hasFlaggedContent;
    }

    /**
     * Bulk scan — runs moderation on all reviews with status "published".
     * Intended to be called from an admin endpoint or a scheduled job.
     *
     * @return count of newly flagged reviews
     */
    @Transactional
    public int runBulkModeration() {
        List<Review> published = reviewRepository.findByStatus("published");
        int flaggedCount = 0;

        for (Review review : published) {
            String contentLower = review.getContent().toLowerCase();
            boolean hasFlaggedContent = PROHIBITED_TERMS.stream()
                    .anyMatch(contentLower::contains);

            if (hasFlaggedContent) {
                review.setStatus("flagged");
                flaggedCount++;
            }
        }

        reviewRepository.saveAll(published);
        log.info("Bulk moderation complete: {} reviews flagged", flaggedCount);
        return flaggedCount;
    }
}
