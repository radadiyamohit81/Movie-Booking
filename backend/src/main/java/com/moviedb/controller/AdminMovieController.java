package com.moviedb.controller;

import com.moviedb.dto.MovieAdminRequest;
import com.moviedb.dto.MovieDto;
import com.moviedb.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only movie management endpoints.
 *
 * WHY @PreAuthorize("hasAuthority('ROLE_ADMIN')") on the class?
 *   Belt-and-suspenders: SecurityConfig already restricts /api/admin/** to
 *   ROLE_ADMIN at the filter chain level. The method-level annotation ensures
 *   that even if the URL pattern is refactored, admin access still requires
 *   the role. Defence in depth.
 */
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminMovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieDto> create(@RequestBody MovieAdminRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> update(@PathVariable Long id,
                                           @RequestBody MovieAdminRequest req) {
        return ResponseEntity.ok(movieService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
