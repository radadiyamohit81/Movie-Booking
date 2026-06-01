package com.moviedb.service;

import com.moviedb.dto.WatchlistRequest;
import com.moviedb.model.Watchlist;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.MovieRepository;
import com.moviedb.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository     movieRepository;

    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlist(Long userId) {
        return watchlistRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    @Transactional
    public Watchlist addToWatchlist(Long userId, WatchlistRequest req) {
        if (!movieRepository.existsById(req.getMovieId())) {
            throw new ResourceNotFoundException("Movie", req.getMovieId());
        }
        if (watchlistRepository.existsByUserIdAndMovieId(userId, req.getMovieId())) {
            throw new IllegalStateException("Movie already in watchlist");
        }

        Watchlist entry = Watchlist.builder()
                .userId(userId)
                .movieId(req.getMovieId())
                .build();

        return watchlistRepository.save(entry);
    }

    @Transactional
    public void removeFromWatchlist(Long userId, Long movieId) {
        if (!watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new ResourceNotFoundException("Watchlist entry not found for movieId: " + movieId);
        }
        watchlistRepository.deleteByUserIdAndMovieId(userId, movieId);
    }
}
