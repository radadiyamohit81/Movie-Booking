package com.moviedb.controller;

import com.moviedb.dto.MovieDto;
import com.moviedb.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Home page aggregation endpoint.
 *
 * Returns a single payload with three movie sections so the React home page
 * needs only one HTTP call on mount.
 *
 * WHY aggregate at the backend instead of letting the frontend call 3 endpoints?
 *   - Reduces 3 round-trips to 1, cutting Time To Interactive.
 *   - The backend can compose the response in parallel (future: CompletableFuture).
 *   - A single cache entry covers the entire home page response.
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<Map<String, List<MovieDto>>> home() {
        List<MovieDto> featured  = movieService.getFeatured(10);
        List<MovieDto> upcoming  = movieService.getUpcoming();
        List<MovieDto> trending  = movieService.getTrending(10);

        return ResponseEntity.ok(Map.of(
                "featured",  featured,
                "upcoming",  upcoming,
                "trending",  trending
        ));
    }
}
