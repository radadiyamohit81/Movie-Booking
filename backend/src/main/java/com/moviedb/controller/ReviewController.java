package com.moviedb.controller;

import com.moviedb.dto.ReviewRequest;
import com.moviedb.entity.Review;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService  reviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Review> createReview(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ReviewRequest req) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(reviewService.createReview(userId, req));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long movieId) {
        return ResponseEntity.ok(reviewService.getReviewsForMovie(movieId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = resolveUserId(principal);
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(id, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}
