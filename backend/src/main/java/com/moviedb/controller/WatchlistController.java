package com.moviedb.controller;

import com.moviedb.dto.WatchlistRequest;
import com.moviedb.model.Watchlist;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserRepository   userRepository;

    @GetMapping
    public ResponseEntity<List<Watchlist>> getWatchlist(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(watchlistService.getWatchlist(resolveUserId(principal)));
    }

    @PostMapping
    public ResponseEntity<Watchlist> add(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody WatchlistRequest req) {
        return ResponseEntity.ok(watchlistService.addToWatchlist(resolveUserId(principal), req));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long movieId) {
        watchlistService.removeFromWatchlist(resolveUserId(principal), movieId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}
