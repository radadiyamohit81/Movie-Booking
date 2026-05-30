package com.moviedb.controller;

import com.moviedb.dto.MovieDto;
import com.moviedb.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public movie browsing and search endpoints.
 *
 * All endpoints are GET — covered by SecurityConfig's permitAll rule for
 * /api/movies/**.
 *
 * Search parameter routing (handled by MovieService.search):
 *   type=title  → title-only search
 *   type=celebs → actor/star search
 *   type=all    → full-text across all fields (default)
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String writer) {

        List<MovieDto> results = movieService.search(
                q, type, minRating, maxRating, genre,
                year, minYear, maxYear, director, writer);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<MovieDto>> getFeatured(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(movieService.getFeatured(limit));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<MovieDto>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(movieService.getByType(type));
    }
}
