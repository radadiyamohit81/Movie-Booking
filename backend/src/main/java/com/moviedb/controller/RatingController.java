package com.moviedb.controller;

import com.moviedb.dto.RatingRequest;
import com.moviedb.model.Rating;
import com.moviedb.model.User;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JWT-protected rating endpoints.
 *
 * WHY @AuthenticationPrincipal UserDetails?
 *   Spring Security injects the currently authenticated principal directly into
 *   the method parameter — no need to manually call SecurityContextHolder.getContext()
 *   .getAuthentication(). The UserDetails object gives us username which we use
 *   to look up the User entity (and thus userId).
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService  ratingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Rating> rate(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RatingRequest req) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ratingService.rateMovie(userId, req));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Rating>> getRatings(@PathVariable Long movieId) {
        return ResponseEntity.ok(ratingService.getRatingsForMovie(movieId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = resolveUserId(principal);
        ratingService.deleteRating(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}
